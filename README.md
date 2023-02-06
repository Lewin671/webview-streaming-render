# webView-streaming-render

There is no specific "streaming interface" in Android's WebView for incrementally rendering a web page.

The purpose of this repo is to show that transferring a streaming data or a complete data to webView
to the webView via shouldInterceptRequest are the same effect. Both of them are be read after all the 
data is received.