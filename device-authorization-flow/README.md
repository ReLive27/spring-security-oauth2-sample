## Relevant Information:

Use the Device Flow if your application runs on a device that is input constrained. For example, a command line application that cannot provide a web browser to users. Unlike most OAuth2 flows, Device Flow does not require using redirect URLs, callbacks, or the client secret. Instead, it requires getting a `device_code`, and then the application’s `client_id` and the `device_code` are used to get an access token.

## Demo

![demo](https://github.com/ReLive27/ReLive27.github.io/blob/main/public/static/images/blogs/oauth2-device.gif)
