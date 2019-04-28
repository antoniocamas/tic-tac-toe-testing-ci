FROM python:3.7-alpine

RUN apk add --update --no-cache --virtual .build-deps \
        g++ \
	   python-dev \	
	   libxml2 \	
	   libxml2-dev && \
	   apk add libxslt-dev && \
	   pip install --no-cache-dir lxml && \
	   apk del .build-deps