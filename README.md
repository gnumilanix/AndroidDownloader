# AndroidDownloader
A demo implementation of file downloader app in Android. The code here shouldn't be used for production grade application. If to be used i strongly suggest to use Service instead of IntentService and post back events using either EventBus of BroadcastReceiver. I will be updating it in a meantime. Here are some supported features:
1.Supports downloading file using ftp/http.
2.Supports to save FTP credential to use for specific FTP server. 
3.Supports monitoring progress in notification bar.
4.Supports pausing/resuming progress in notification bar.
5.Supports notification aggregating and big picture for picture.
6.Supports controlling download over wifi or mobile.
7.Supports limiting of max download size. 
8.Supports batch pause/resume/delete.
9.Supports thirdparty apps to send download intent.
10.Supports both phones and tablets.
11.Supports persistance storage of download information in SQLiteDB using content provider.
11.Supports filtering and sorting of files by size, type (audio, video and others.)
