
def main():
    target_file = './sdk/spring/tree.txt'
    with open(target_file, 'r') as file:
        for line in file:
            if line.startswith('[INFO]'):
                print(line, end = '')

if __name__ == '__main__':
    main()