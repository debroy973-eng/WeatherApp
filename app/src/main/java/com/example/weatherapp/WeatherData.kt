package com.example.weatherapp

import java.util.*

class WeatherData(private var mTemp:String, private var mDescrip:String, private var mPlace:String,
                  private var mDate:String, private var mTime:String,private var mIcon:String) {
    constructor(temp: String, descrip: String, date: String, time: String,icon:String):this(temp,descrip,null.toString(),date,time,icon) {
        this.mTemp=temp
        this.mDescrip=descrip
        this.mDate=date
        this.mTime=time
        this.mIcon=icon
    }
    public fun getmTemp():String{
        return mTemp
    }
    public fun getmPlace():String{
        return mPlace
    }
    public fun getmDescrip():String{
        return mDescrip
    }
    public fun getmDate():String{
        return mDate
    }
    public fun getmTime():String{
        return mTime
    }
    public fun getmIcon():String{
        return mIcon
    }
}