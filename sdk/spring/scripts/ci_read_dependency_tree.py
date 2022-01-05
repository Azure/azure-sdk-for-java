# Python version 3.4 or higher is required to run this script.
#
# This script is used to print dependency tree in pipeline.
#
# The script must be run at the root of azure-sdk-for-java.
def main():
    target_file = './sdk/spring/dependency_tree.txt'
    with open(target_file, 'r') as file:
        for line in file:
            if line.startswith('[INFO]'):
                print(line, end = '')

if __name__ == '__main__':
    main()
