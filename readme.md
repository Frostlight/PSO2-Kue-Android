# PSO2 Kue
PSO2 Kue is an a event tracker Android app for the Japanese PC MMO Phantasy Star Online. It times upcoming events (emergency quests) and keep the player notified about when they will occur.

##  Quests
> There are two forms of Emergency Quests, ones pre-scheduled by SEGA themselves, and ones randomly chosen by your server. Due to this, the calendar is unable to predict whenever random Emergency Quests are going to occur. However, to get around this, premium users with PSO2es (the mobile app associated with this MMO) installed  will be given announcements to Emergency Quests 1 hour in advance. By pulling these push notifications, the community is able to check if an Emergency Quest is going to occur 1 hour before they happen.

Source: http://www.bumped.org/psublog/pso2-jp-extended-maintenance-172015/

The goal of this app is to aggregate both types of emergency quests into a list display by:

1. periodically scanning certain Twitter bots for pulled random emergency quest notifications (in Japanese) and
2. updating the scheduled emergency quest timetable using an already-translated schedule when necessary.

The timetable will is kept track of by a backend SQLite database.  

A translation table is used for the Japanese Twitter updates. Alternatively, Hablaa translate if no translation is available, or just use the original Japanese updates.  

### Version
1.13

### Tech
PSO2 Kue uses a number of freely available technologies to work properly:
- [SQLite] - A lightweight backend database for storing the emergency quest schedule and translation table. Emergency quests that have already passed will be wiped from the database
- [Twitter4j] - An unofficial Java library for working with the Twitter API
- [Joda-Time] - A quality replacement for the Java Time and Date classes which fixes much of the unnecessary convolution in the way Java handles time (if Android development moves to Java SE 8, migrate to java.time instead)
- [Microsoft Translator Java API] - A java wrapper used for Bing translator
- Google App Engine - Used to host the backend for sending push notifications to Android devices

###APIs
PSO2 will use the following APIs:
- [Twitter REST API] - Used to periodically fetch emergency quest notifications
- [Google Calender] - Used to access and import a translated version of the emergency quest schedule posted at https://www.google.com/calendar/embed?src=pso2emgquest%40gmail.com
- [Bing Translation API] - Used if there is no translation available

[SQLite]: https://www.sqlite.org/
[Twitter Streaming API]: https://dev.twitter.com/overview/documentation
[Twitter REST API]: https://dev.twitter.com/rest/public
[Google Calender]: https://developers.google.com/google-apps/calendar/
[Google Spreadsheet API]: https://developers.google.com/google-apps/spreadsheets/
[Bing Translation API]: http://www.bing.com/dev/translator
[Microsoft Translator Java API]: https://code.google.com/p/microsoft-translator-java-api/
[Twitter4j]: http://twitter4j.org/en/index.html
[Joda-Time]: http://www.joda.org/joda-time/
