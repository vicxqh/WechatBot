# WechatBot
##A wechat robot running on android devices.

Based on [xposed framework](https://github.com/rovo89/XposedBridge/wiki/Development-tutorial) and this specific [wechat](https://github.com/xiaowei1235/WechatBot/blob/master/weixin637android660.apk) version, I plan to implement a bot that can:

be informed when received new message of :
- [x] Text
- [ ] Picture
- [ ] Audio

and get the content of this new message:
- [x] Text
- [ ] Picture
- [ ] Audio

and have the alibity to send the following kind of message to both friends and groups:
- [x] Text
- [ ] Picture
- [ ] Audio

##Limitation
Since this work will be done by hooking the Java interfaces of wechat and the Java code of wechat is of cause obfuscated, the methods or fileds I hooked in this work may not work on other wechat versions, but the idea is same. For your convenience, I will post all my notes on the [wiki](https://github.com/xiaowei1235/WechatBot/wiki) pages.
