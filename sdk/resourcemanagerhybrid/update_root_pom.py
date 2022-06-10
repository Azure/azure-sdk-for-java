with open('pom.xml', 'r') as file:
  filedata = file.read()

filedata = filedata.replace('<module>sdk/resourcemanager</module>', '<module>sdk/resourcemanagerhybrid</module>')

with open('pom.xml', 'w') as file:
  file.write(filedata)
