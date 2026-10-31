# Room — entities, DAOs, DB accessed via generated code; keep model + DAO signatures.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep class com.quotewidget.data.Quote { *; }
-keep class com.quotewidget.data.QuoteDao { *; }
-keep class com.quotewidget.data.AppDatabase { *; }

# WorkManager — Worker instantiated by reflection from WorkManager.
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class com.quotewidget.widget.RotateWorker { *; }

# Widget / receivers — referenced from AndroidManifest, instantiated by system.
-keep class com.quotewidget.widget.QuoteWidgetProvider { *; }
-keep class com.quotewidget.widget.BootReceiver { *; }
-keep class com.quotewidget.QuoteWidgetApplication { *; }
