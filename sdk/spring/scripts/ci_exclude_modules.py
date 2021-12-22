# python .\sdk\spring\scripts\ci_exclude_modules.py
import time
import os

unsupported_list = ['com.azure.spring:spring-cloud-azure-stream-binder-test']

def exclude_modules():
    file_path = './eng/versioning/version_client.txt'
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
        for i in unsupported_list:
            pos = content.find(i)
            if pos != -1:
                content = content[:pos] +'# '+ content[pos:]
                with open(file_path, "r+", encoding="utf-8") as f:
                    f.writelines(content)

def main():
    start_time = time.time()
    print('Current working directory = {}.'.format(os.getcwd()))
    exclude_modules()
    elapsed_time = time.time() - start_time
    print('elapsed_time = {}'.format(elapsed_time))

if __name__ == '__main__':
    main()
