FROM ibmcom/cloudant-developer

ADD populate.py requirements.txt /tmp/

RUN curl "https://bootstrap.pypa.io/get-pip.py" -o "get-pip.py"
RUN python get-pip.py
RUN pip install -r requirements.txt
