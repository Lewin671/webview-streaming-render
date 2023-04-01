# webView-streaming-render

There is no specific "streaming interface" in Android's WebView for incrementally rendering a web page.

The purpose of this repo is to show that transferring a streaming data or a complete data to the webView via shouldInterceptRequest are the same effect.
That is, WebView content will start to process data(such as parsing HTML, CSS, Javascript etc.) until the data is fully read, even though you provide an streaming inputStream to WebView content via shouldInterceptRequest .