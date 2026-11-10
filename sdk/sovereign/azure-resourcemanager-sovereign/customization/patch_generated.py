"""Reapply the package-local terminal remediation envelope customization after generation."""
from pathlib import Path

path = (
    Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "azure"
    / "resourcemanager" / "sovereign" / "fluent" / "models" / "SovereignViewRemediateResultInner.java"
)
text = path.read_text(encoding="utf-8")
if "String status = null;" not in text:
    replacements = [
        ("                = new SovereignViewRemediateResultInner();",
         "                = new SovereignViewRemediateResultInner();\n"
         "            SovereignViewRemediateResultInner properties = null;\n"
         "            String status = null;"),
        ('                    deserializedSovereignViewRemediateResultInner.error = ManagementError.fromJson(reader);',
         '                    deserializedSovereignViewRemediateResultInner.error = ManagementError.fromJson(reader);\n'
         '                } else if ("status".equals(fieldName)) {\n'
         '                    status = reader.getString();\n'
         '                } else if ("properties".equals(fieldName)) {\n'
         '                    properties = SovereignViewRemediateResultInner.fromJson(reader);'),
        ("            return deserializedSovereignViewRemediateResultInner;",
         '            if ("Succeeded".equalsIgnoreCase(status) && properties != null) {\n'
         '                if (deserializedSovereignViewRemediateResultInner.remediationId == null) {\n'
         '                    deserializedSovereignViewRemediateResultInner.remediationId = properties.remediationId;\n'
         '                }\n'
         '                if (deserializedSovereignViewRemediateResultInner.error == null) {\n'
         '                    deserializedSovereignViewRemediateResultInner.error = properties.error;\n'
         '                }\n'
         '            }\n'
         '            return deserializedSovereignViewRemediateResultInner;'),
    ]
    for before, after in replacements:
        if text.count(before) != 1:
            raise RuntimeError(f"Generated source changed; review customization anchor: {before}")
        text = text.replace(before, after)
    path.write_text(text, encoding="utf-8", newline="\n")
print("Terminal remediation envelope customization is present.")
