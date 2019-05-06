package org.cafydia.android.core;

import org.cafydia.android.util.MyRound;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by usuario on 2/03/14.
 */
public class Instant {
    // constants
    private static final SimpleDateFormat mDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private static final SimpleDateFormat mUserDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static final SimpleDateFormat mUserUsDateFormat = new SimpleDateFormat("MM-dd-yyyy");

    private static final SimpleDateFormat mUserTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat mUserTimeFormatShort = new SimpleDateFormat("HH:mm");

    private static final SimpleDateFormat mUserDateFormatShort = new SimpleDateFormat("dd-MM-yy");
    private static final SimpleDateFormat mUserUsDateFormatShort = new SimpleDateFormat("MM-dd-yy");

    private static String mLanguage;

    private Calendar c = Calendar.getInstance();

    // attributes
    private Date date;

    private void checkInstant(){
        /*
        if(date != null) {
            long now = System.currentTimeMillis();
            Float result = ((date.getTime() - now) / (float) (1000 * 60 * 60 * 24));

            if (result < -500) {
                Log.d("getDaysPassedFromNow", "Instant: " + Long.toString(date.getTime()) + " (" + getInternalDateTimeString() + ")" + " Now: " + Long.toString(now), new Exception("getDaysPassedFromNow"));
            }
        }
        */
    }

    // constructors
    public Instant () {
        date = new Date();
        c.setTime(date);

        checkInstant();
    }
    public Instant(int daysIndex){
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.DATE, daysIndex);
        date = c.getTime();

        checkInstant();
    }

    public Instant(float daysIndex){
        long m = (long) (daysIndex * 24 * 60 * 60 * 1000);
        c.setTimeInMillis(System.currentTimeMillis() + m);
        date = c.getTime();

        // todo aquí la cagamos

        checkInstant();
    }

    public Instant(long millis){
        c.setTimeInMillis(millis);
        date = c.getTime();

        checkInstant();
    }

    public Instant (String dateString){
        if(dateString.equals("")){
            date = null;
        } else {
            try {
                this.date = mDatetimeFormat.parse(dateString);
                c.setTime(date);
            } catch (Exception e) {
                try {
                    this.date = mDateFormat.parse(dateString);
                } catch (Exception e2) {
                    try {
                        this.date = mUserDateFormat.parse(dateString);
                        c.setTime(date);
                    } catch (Exception e3) {
                    }
                }
            }
        }

        // todo la cagamos aquí

        checkInstant();
    }

    public Instant(Instant i){
        if(i != null && i.toDate() != null) {
            date = i.toDate();

        } else {
            date = new Date(0);
        }
        c.setTime(date);

        checkInstant();
    }


    public Instant setTimeToTheStartOfTheDay(){
        if(date != null) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
        }
        return this;
    }
    public Instant setTimeToTheMorning(){
        if(date != null) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 5);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
        }
        return this;
    }
    public Instant setTimeToTheMorningOnMonday(){
        if(date != null) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 5);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
            while(!this.getDayOfWeek().equals(0)) {
                this.decreaseOneDay();
            }
            c.setTime(date);
        }
        return this;
    }
    public Instant setTimeToTheEndOfTheDay(){
        if(date != null) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
        }
        return this;
    }

    public Instant increaseOneDay(){
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 1);
        date = c.getTime();
        return this;
    }

    public Instant decreaseOneDay(){
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -1);
        date = c.getTime();
        return this;
    }

    public Instant increaseNDays(int n){
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, n);
        date = c.getTime();
        return this;
    }
    public Instant decreaseNDays(int n){
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -n);
        date = c.getTime();
        return this;
    }
    public Instant increaseNMinutes(int n){
        c.setTime(date);
        c.add(Calendar.MINUTE, n);
        date = c.getTime();
        return this;
    }
    public Instant decreaseNMinutes(int n){
        c.setTime(date);
        c.add(Calendar.MINUTE, -n);
        date = c.getTime();
        return this;
    }

    public Instant increaseOneMonth(){
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        date = c.getTime();
        return this;
    }
    public Instant decreaseOneMonth(){
        c.setTime(date);
        c.add(Calendar.MONTH, -1);
        date = c.getTime();
        return this;
    }


    public Instant advanceToToday(){
        c.setTimeInMillis(System.currentTimeMillis());
        date = c.getTime();
        return this;

    }

    public String getInternalDateTimeString(){
        return date.getTime() == 0 ? "" : mDatetimeFormat.format(date);
    }
    public String getInternalDateString(){
        return date.getTime() == 0 ? "" : mDateFormat.format(date);
    }

    public Integer getDayOfWeek(){
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        dayOfWeek -= 2;
        if (dayOfWeek < 0) {
            dayOfWeek += 7;
        }
        return dayOfWeek; // 0 monday, 6 sunday.
    }

    public String getUserDateString(){
        mLanguage = Locale.getDefault().getISO3Language();

        if (mLanguage.equals("spa")) {
            return date.getTime() == 0 ? "" : mUserDateFormat.format(date);
        } else {
            return date.getTime() == 0 ? "" : mUserUsDateFormat.format(date);
        }
    }
    public String getUserDateStringShort(){
        mLanguage = Locale.getDefault().getISO3Language();

        if (mLanguage.equals("spa")) {
            return date.getTime() == 0 ? "" : mUserDateFormatShort.format(date);
        } else {
            return date.getTime() == 0 ? "" : mUserUsDateFormatShort.format(date);
        }
    }
    public String getUserTimeString(){
        return date.getTime() == 0 ? "" : mUserTimeFormat.format(date);
    }
    public String getUserTimeStringShort(){
        return date.getTime() == 0 ? "" : mUserTimeFormatShort.format(date);
    }

    public Instant setYearMonthDay(Integer year, Integer month, Integer day){
        c.set(year, month, day);
        date = c.getTime();
        return this;
    }

    public Instant setInstant(Instant i){
        date = i.toDate();
        c.setTime(date);
        return this;
    }

    public Instant setTimeInMilis(long milis){
        date.setTime(milis);
        c.setTime(date);
        return this;
    }


    /*
     * < 0, instante pasado, es que el objeto instanciado, es anterior a ahora mismo.
     * = 0, instante actual, es que el objeto instanciado es idéntico a ahora mismo.
     * > 0, instante futuro, es que el objeto instanciado, es posterior a ahora mismo.
     */
    public Float getDaysPassedFromNow(){
        if(date == null) return null;

        long now = System.currentTimeMillis();
        Float result = ((date.getTime() - now) / (float) (1000 * 60 * 60 * 24));

        checkInstant();

        return result;
    }

    public boolean isToday(){
        return MyRound.round(getDaysPassedFromNow(), 0) == 0;
    }

    /*
     * < 0, o números negativos, es que el objeto instanciado, es anterior al parámetro pasado.
     * = 0, es que el objeto instanciado es idéntico al parámetro pasado.
     * > 0, o números positivos, es que el objeto instanciado, es posterior al parámetro pasado.
     */
    public Float getDaysPassedFromInstant(Instant i){
        if(date == null) return null;

        long difference = date.getTime() - i.toDate().getTime();
        return difference / (float) (1000 * 60 * 60 * 24);
    }

    public Boolean isBetweenDates(Date startdate, Date enddate){
        if(date == null) return null;

        long startDifference, endDifference;

        startDifference = date.getTime() - startdate.getTime();
        endDifference = date.getTime() - enddate.getTime();

        return startDifference > 0 && endDifference < 0;
    }
    public Boolean isBetweenInstants(Instant startdate, Instant enddate){
        if(date == null) return null;

        long startDifference, endDifference;

        startDifference = date.getTime() - startdate.toDate().getTime();
        endDifference = date.getTime() - enddate.toDate().getTime();

        return startDifference > 0 && endDifference < 0;
    }

    // the year
    public Integer getYear(){
        if(date == null) { return null; }
        c.setTime(date);
        return c.get(Calendar.YEAR);
    }

    // 1 is first day of month
    public Integer getDay(){
        if(date == null) { return null; }
        c.setTime(date);
        return c.get(Calendar.DAY_OF_MONTH);
    }

    // 0 is january
    public Integer getMonth(){
        if(date == null) { return null; }
        c.setTime(date);
        return c.get(Calendar.MONTH);
    }

    public Instant setHourAndMinute(int hour, int minute){
        if(date != null) {
            c.setTime(date);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            date = c.getTime();
        }
        return this;
    }

    public Integer getHour(){
        if(date == null) { return null; }
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY);
    }

    public Integer getMinute(){
        if(date == null) { return null; }
        c.setTime(date);
        return c.get(Calendar.MINUTE);
    }

    public Integer getMinutesPassedFromMidnight(){
        if(date == null) { return null; }
        c.setTime(date);

        return (getHour() * 60) + getMinute();
    }

    public Date toDate(){
        if(date != null) {
            return new Date(date.getTime());
        } else {
            return null;
        }
    }

    ////////
    //////// for Charts
    ////////
    public Integer getMinutesPassedSince5AM(){
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 5);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date am5 = c.getTime();

        long now = date.getTime();
        long am5time = am5.getTime();

        int result = (int) ((now - am5time) / (60 * 1000));

        result = result < 0 ? result + (24 * 60) : result;

        // if result is negative, we add one day in minutes
        return result;
    }

    public Integer getMinutesPassedSince5AMMonday(){
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.set(Calendar.HOUR_OF_DAY, 5);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Date monday5AM = c.getTime();

        long now = date.getTime();
        long monday5AMTime = monday5AM.getTime();

        int result = (int) ((now - monday5AMTime) / (60 * 1000));
        return result < 0 ? result + (24 * 60 * 7) : result;
    }

    // Si le pasas un objeto meal a una glucosa
    // por ejem:

    // GlucoseTest g;
    // Meal m;

    // g.getMinutesPassedSinceHourAndMinuteInstant(m);

    // si da positivo es que la glucosa es posterior a la comida
    // si da negativo es que la glucosa es anterior a la comida

    // con el int devuelto sería:

    // int minutes = g.getMinutesPassedSinceHourAndMinuteInstant(m);

    // String hourString;
    // int h = Math.abs(minutes) / 60; // horas
    // int m = Math.abs(minutes) % 60; // minutos

    // if (minutes > 0) {
    //      hourString = h + ":" + m; // String
    // } else {
    //      hourString = "-" + h + ":" + m; // String
    // }
    //
    //

    public Integer getMinutesPassedSinceHourAndMinuteInstant(Instant i){
        float df = getDaysPassedFromInstant(i);
        int d = getDaysPassedFromInstant(i).intValue();

        float residual = df - (float) d;

        return (int) (residual * 24 * 60);
    }

}
