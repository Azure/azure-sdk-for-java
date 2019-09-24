FROM python:3
WORKDIR /app

# Copy requirements.txt and install
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# Copy sources
COPY . ./

ENTRYPOINT ["python", "./app.py"]
