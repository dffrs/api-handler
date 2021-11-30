# api-handler
# V1.0

Simple Java libary that makes an API Call as simple it could be - calling, just, one method.
For that, it uses a configuration file, where is stored all the API's configuration options (host, headers, endpoint, etc). It was designed this way to accummodate
some level of security and flexibility, when dealing with API's configurations.
It caches the result, to be as efficient and lightweight (to the API's host) as possible, if it is detected that a similar call was made, previously.

# NOTE:
      For now, it only supports GET HTTP Requests, but, if it proves necessary, later versions will implement other HTTP Requests.
      Library desinged (and tested) to work with [RAPID API](https://rapidapi.com/hub)'s available APIs.
