FROM python:3-alpine
ADD populate.py requirements.txt /tmp/
RUN pip3 install -r tmp/requirements.txt
ENTRYPOINT ["python3", "tmp/populate.py"]
CMD ["localhost", "8080"]
