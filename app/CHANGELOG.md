#####0.092#####
changes:

 - theme changes
 - should fix possible crash that happens when stream data is fetched in the background
 - background stream data increased from 15 minutes to an hour

#####0.091#####
changes:

- bulk insert of events and channels
- channel and event data properly fetched every 15 minutes

#####0.09#####
changes:

 - crash fix on Lollipop devices
 - rework channel and event fetching
 - refresh button added
 - ensure no duplicate days/events in events list

#####0.082#####
changes:

 - crash fix

#####0.081#####
changes:

 - added crashlytics

#####0.08#####
changes:

 - user-agent sent on requests
 - okhttp instead of httpurlconnection
 - picasso instead of universal image loader

#####0.07#####
changes:

 - check for internet connection before tasks that require one
 - other misc fixes

#####0.06#####
changes:

 - handle umlauts in event names
 - event list will update after task to get channel info is completed
 - flags to indicate event broadcast language if applicable

#####0.051#####
changes:

 - fixes crashes regarding mini controller when switching fragments
 - small layout changes

#####0.05#####
changes:

 - hd badged changed to text instead of image
 - added tabs with list of events

#####0.04#####
changes:

 - channels display an event title when there is an event currently airing
 - events broadcasted in 720p are marked

#####0.03#####
changes:

 - channels are added to channel list as soon as they are parsed (backend change: now using content provider)
 - better handling of the app on devices that do not have google services (e.g. Fire TV)

#####0.02#####
changes:

 - softkeys hide on tablets
 - checks to make sure credentials are set
 - slightly better launcher icon
 - do not start video player if returning from another activity (i.e., chromecast controller or external player)
