# Document Service


## Available services:

### `html2pdf`
`/api/v1/html2pdf`

example

`curl -i http://localhost:9812/api/v1/html2pdf -d '{"html":"<html><head><title>ex</title></head><body><h1>Hello World</h1></body></html>"}'`

# Available Tools

### `Consumer`

Will invoke service with html file supplied

### How-to install consumer tool

* clone this respository
* run script `./install-consumer.sh` - this will create the following command for you to use in the terminal `html2pdf`

The first time you run the tool you will have to supply which html file to use and where the generated pdf should be stored.
Next time you run the tool, the tool will remember your previous selections, then you just can press `Enter` as long as you want to you the same values.

ex first run:
````
> html2pdf
service endpoint [http://localhost:9812/api/v1/html2pdf] >
input file > /Users/test/html2pdf.html
output file > /Users/test/out.pdf
````

ex second run:
````
> html2pdf
service endpoint [http://localhost:9812/api/v1/html2pdf] >
input file [/Users/test/html2pdf.html] >
output file [/Users/test/out.pdf] > 
````
