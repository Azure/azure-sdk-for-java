# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

from copy import deepcopy
from pathlib import Path
import re
import xml.etree.ElementTree as ET

from utils import include_update_marker, load_version_overrides, version_update_marker


namespace = '{http://maven.apache.org/POM/4.0.0}'
ET.register_namespace('', namespace[1:-1])
ET.register_namespace('xsi', 'http://www.w3.org/2001/XMLSchema-instance')


def parse_pom(path):
    return ET.parse(path, parser=ET.XMLParser(target=ET.TreeBuilder(insert_comments=True)))


def text(element, name):
    child = element.find(namespace + name)
    return child.text.strip() if child is not None and child.text else None


def identifier(element):
    group = text(element, 'groupId')
    if not group:
        parent = element.find(namespace + 'parent')
        group = text(parent, 'groupId') if parent is not None else None
    artifact = text(element, 'artifactId')
    if not group or not artifact:
        raise ValueError('Missing Maven groupId or artifactId.')
    return group + ':' + artifact


def ensure(element, name):
    child = element.find(namespace + name)
    if child is None:
        child = ET.SubElement(element, namespace + name)
    return child


def set_text(element, name, value):
    ensure(element, name).text = value


def dependency_key(dependency):
    return (identifier(dependency), text(dependency, 'type') or 'jar', text(dependency, 'classifier'))


def update_markers(root, overrides):
    for container in root.iter():
        previous = None
        for child in container:
            if child.tag is ET.Comment:
                marker = version_update_marker.search(child.text or '') or include_update_marker.search(child.text or '')
                if marker and marker.group(2) == 'external_dependency' and marker.group(1) in overrides:
                    if previous is None:
                        raise ValueError('Dependency version marker does not follow an XML element.')
                    module = overrides[marker.group(1)]
                    previous.text = (module.string_for_allowlist_include()
                                     if include_update_marker.search(child.text or '') else module.external_dependency)
            else:
                previous = child


def parent_roots(path, root, projects):
    parents = []
    seen = {path}
    while (parent := root.find(namespace + 'parent')) is not None:
        parent_id = identifier(parent)
        if parent_id in projects:
            path, root = projects[parent_id]
        else:
            relative = text(parent, 'relativePath')
            if relative is None:
                relative = '../pom.xml'
            path = (path.parent / relative).resolve()
            if path.is_dir():
                path /= 'pom.xml'
            if not relative or not path.is_file():
                raise ValueError('Parent {} must be available locally to isolate dependency overrides.'.format(parent_id))
            root = parse_pom(path).getroot()
            if identifier(root) != parent_id:
                raise ValueError('Parent POM {} does not match {}.'.format(path, parent_id))
        if path in seen:
            raise ValueError('Cyclic parent POM hierarchy at {}.'.format(path))
        seen.add(path)
        parents.append(root)
    return parents


def prepare_root(root, parents, overrides):
    inherited = {}
    for parent in reversed(parents):
        for dependency in parent.findall(namespace + 'dependencies/' + namespace + 'dependency'):
            if identifier(dependency) in overrides:
                inherited[dependency_key(dependency)] = dependency
        for dependency in parent.findall(
                namespace + 'profiles/' + namespace + 'profile/' + namespace + 'dependencies/' + namespace + 'dependency'):
            if identifier(dependency) in overrides:
                raise ValueError('Cannot isolate a version override inherited from a parent profile: {}.'.format(
                    identifier(dependency)))

        properties = parent.find(namespace + 'properties')
        if properties is not None:
            copied = deepcopy(properties)
            update_markers(copied, overrides)
            for old, new in zip(properties, copied):
                if old.tag is not ET.Comment and old.text != new.text:
                    target = ensure(root, 'properties').find(new.tag)
                    if target is None:
                        target = ET.SubElement(ensure(root, 'properties'), new.tag)
                    target.text = new.text

    dependencies = root.find(namespace + 'dependencies')
    own_dependencies = ({dependency_key(d) for d in dependencies.findall(namespace + 'dependency')}
                        if dependencies is not None else set())
    for key, dependency in inherited.items():
        if key not in own_dependencies:
            ensure(root, 'dependencies').append(deepcopy(dependency))

    update_markers(root, overrides)
    for dependency in root.iter(namespace + 'dependency'):
        module = overrides.get(identifier(dependency))
        if module is not None:
            set_text(dependency, 'version', module.external_dependency)

    managed = ensure(ensure(root, 'dependencyManagement'), 'dependencies')
    existing = {dependency_key(d) for d in managed.findall(namespace + 'dependency')}
    for module in overrides.values():
        if (module.name, 'jar', None) not in existing:
            dependency = ET.SubElement(managed, namespace + 'dependency')
            set_text(dependency, 'groupId', module.group_id)
            set_text(dependency, 'artifactId', module.artifact_id)
            set_text(dependency, 'version', module.external_dependency)

    plugins = ensure(ensure(root, 'build'), 'plugins')
    enforcer = next((plugin for plugin in plugins.findall(namespace + 'plugin')
                     if text(plugin, 'artifactId') == 'maven-enforcer-plugin'
                     and text(plugin, 'groupId') in (None, 'org.apache.maven.plugins')), None)
    if enforcer is None:
        enforcer = ET.SubElement(plugins, namespace + 'plugin')
        set_text(enforcer, 'groupId', 'org.apache.maven.plugins')
        set_text(enforcer, 'artifactId', 'maven-enforcer-plugin')
    includes = ensure(ensure(ensure(ensure(enforcer, 'configuration'), 'rules'), 'bannedDependencies'), 'includes')
    includes.set('combine.children', 'append')
    for module in overrides.values():
        value = module.string_for_allowlist_include()
        if not any(include.text == value for include in includes.findall(namespace + 'include')):
            ET.SubElement(includes, namespace + 'include').text = value


def prepare_project_overrides(aggregate_pom, project_list, overrides_file, override_name):
    overrides = {}
    load_version_overrides(overrides_file, overrides, override_name)
    if not overrides:
        raise ValueError('Dependency version override {} is empty.'.format(override_name))
    for module in overrides.values():
        if not module.external_dependency:
            raise ValueError('Dependency override version is empty for {}.'.format(module.name))
        if module.name != module.string_for_allowlist_include().rsplit(':', 1)[0]:
            raise ValueError('Project-scoped overrides require canonical groupId:artifactId keys: {}.'.format(module.name))
    selected = {project.strip() for project in project_list.split(',')}
    if not selected or '' in selected:
        raise ValueError('Project list cannot be empty.')

    aggregate_pom = Path(aggregate_pom).resolve()
    projects = {}
    for module in parse_pom(aggregate_pom).getroot().findall(namespace + 'modules/' + namespace + 'module'):
        path = (aggregate_pom.parent / module.text.strip().lstrip('/\\')).resolve()
        if path.is_dir():
            path /= 'pom.xml'
        root = parse_pom(path).getroot()
        project_id = identifier(root)
        if project_id in projects:
            raise ValueError('Duplicate project {} in {}.'.format(project_id, aggregate_pom))
        projects[project_id] = (path, root)

    missing = selected - projects.keys()
    if missing:
        raise ValueError('Projects not found in {}: {}.'.format(aggregate_pom, ', '.join(sorted(missing))))
    parents = {identifier(parent) for _, root in projects.values()
               if (parent := root.find(namespace + 'parent')) is not None}
    if selected & parents:
        raise ValueError('Cannot isolate overrides on shared parent projects: {}.'.format(
            ', '.join(sorted(selected & parents))))

    prepared = []
    for project_id in sorted(selected):
        path, original = projects[project_id]
        if text(original, 'packaging') == 'pom':
            raise ValueError('Dependency overrides require a leaf library or test project: {}.'.format(project_id))
        root = deepcopy(original)
        prepare_root(root, parent_roots(path, original, projects), overrides)
        ET.indent(root, space='  ')
        original_text = path.read_text(encoding='utf-8')
        project_start = re.search(r'<(?:[\w.-]+:)?project(?:\s|>)', original_text)
        if project_start is None:
            raise ValueError('Missing Maven project element in {}.'.format(path))
        prepared.append((path, original_text[:project_start.start()] + ET.tostring(root, encoding='unicode') + '\n'))
    for path, contents in prepared:
        path.write_text(contents, encoding='utf-8')
        print('Prepared {} with dependency override {}.'.format(path, override_name))
