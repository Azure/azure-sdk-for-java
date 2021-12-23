# python .\sdk\spring\scripts\ci_exclude_modules.py
import time
import os

unsupported_list = ['com.azure.spring:spring-cloud-azure-stream-binder-test',
                    'com.azure.spring:spring-cloud-azure-stream-binder-servicebus',
                    'com.azure.spring:spring-cloud-azure-stream-binder-eventhubs']

def exclude_modules_in_txt():
    file_path = './eng/versioning/version_client.txt'
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
        for i in unsupported_list:
            pos = content.find(i)
            if pos != -1:
                print("processing:" + file_path)
                print("noted unsupported module")
                content = content[:pos] +'# '+ content[pos:]
                with open(file_path, "r+", encoding="utf-8") as f:
                    f.writelines(content)

def exclude_modules_in_pom():
    unsupported_list_for_pom = []
    for i in unsupported_list:
        unsupported_list_for_pom.append(i.split(':')[1])

    file_path = './sdk/spring/pom.xml'
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
        for i in unsupported_list_for_pom:
            pos = content.find(i)
            if pos != -1:
                print("processing:" + file_path)
                print("noted unsupported module")
                content = content[:pos-8] +'<!--'+ content[pos-8:]
                content = content[:pos+len(i)+13] +'-->'+ content[pos+len(i)+13:]
                with open(file_path, "r+", encoding="utf-8") as f:
                    f.writelines(content)

def exclude_modules_in_yml():
    file_path = './sdk/spring/ci.yml'
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()

def main():
    start_time = time.time()
    print('Current working directory = {}.'.format(os.getcwd()))
    exclude_modules_in_pom()
    exclude_modules_in_txt()
    exclude_modules_in_yml()
    elapsed_time = time.time() - start_time
    print('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
