package com.rukiasoft.androidapps.cocinaconroll;



        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.appwidget.AppWidgetProvider;
        import android.content.Context;
        import android.content.Intent;
        import android.widget.RemoteViews;

        import com.rukiasoft.androidapps.cocinaconroll.ui.RecipeListActivity;
        import com.rukiasoft.androidapps.cocinaconroll.utilities.Constants;
        import com.rukiasoft.androidapps.cocinaconroll.utilities.Tools;

public class MyWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            //ID del widget actual
            //Actualizamos el widget actual
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    public static void updateWidget(Context context,
                                        AppWidgetManager appWidgetManager, int widgetId)
    {


        //Obtenemos la lista de controles del widget actual
        RemoteViews controls =
                new RemoteViews(context.getPackageName(), R.layout.my_widget);

        //Actualizamos el mensaje en el control del widget
        Tools mTools = new Tools();
        int desserts = mTools.getIntegerFromPreferences(context, Constants.PROPERTY_NUMBER_DESSERTS);
        int mains = mTools.getIntegerFromPreferences(context, Constants.PROPERTY_NUMBER_MAIN);
        int starters = mTools.getIntegerFromPreferences(context, Constants.PROPERTY_NUMBER_STARTERS);
        controls.setTextViewText(R.id.number_starters, context.getResources().getString(R.string.starters)
                .concat(":")
                .concat(String.valueOf(starters)));
        controls.setTextViewText(R.id.number_main, context.getResources().getString(R.string.main_courses)
                .concat(":")
                .concat(String.valueOf(mains)));
        controls.setTextViewText(R.id.number_desserts, context.getResources().getString(R.string.desserts)
                .concat(":")
                .concat(String.valueOf(desserts)));

        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, RecipeListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        controls.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);


        //Notificamos al manager de la actualización del widget actual
        appWidgetManager.updateAppWidget(widgetId, controls);
    }
}