package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(val list:ArrayList<WeatherData>): RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>(){

    private lateinit var context:Context
    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val currentItem = list[position]
        holder.temparetureTextView.text = currentItem.getmTemp()
        holder.descriptionTextView.text = currentItem.getmDescrip()
        holder.dateTextView.text = currentItem.getmDate()
        holder.timeTextView.text = currentItem.getmTime()
        /*Glide.with(context)
            .load("http://openweathermap.org/img/wn/"+currentItem.getmIcon()+"@2x.png")
            .centerCrop()
            .into(holder.imageView)*/
        when(currentItem.getmIcon()){
            "01d"->holder.imageView.setImageResource(R.drawable.clear_sky_morning)
            "01d"->holder.imageView.setImageResource(R.drawable.clear_sky_night)
            "02d"->holder.imageView.setImageResource(R.drawable.few_clouds_morning)
            "02n"->holder.imageView.setImageResource(R.drawable.few_clouds_night)
            "03d"->holder.imageView.setImageResource(R.drawable.scattered_clouds)
            "03n"->holder.imageView.setImageResource(R.drawable.scattered_clouds)
            "04d"->holder.imageView.setImageResource(R.drawable.broken_clouds)
            "04n"->holder.imageView.setImageResource(R.drawable.broken_clouds)
            "09d"->holder.imageView.setImageResource(R.drawable.shower_rain)
            "09n"->holder.imageView.setImageResource(R.drawable.shower_rain)
            "10d"->holder.imageView.setImageResource(R.drawable.rain_morning)
            "10n"->holder.imageView.setImageResource(R.drawable.rain_night)
            "11d"->holder.imageView.setImageResource(R.drawable.thunderstorm)
            "11n"->holder.imageView.setImageResource(R.drawable.thunderstorm)
            "13d"->holder.imageView.setImageResource(R.drawable.snow)
            "13n"->holder.imageView.setImageResource(R.drawable.snow)
            "50d"->holder.imageView.setImageResource(R.drawable.mist)
            else->holder.imageView.setImageResource(R.drawable.mist)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val item=LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return WeatherViewHolder(item)
    }


    class WeatherViewHolder(view:View): RecyclerView.ViewHolder(view){
        val temparetureTextView:TextView=view.findViewById(R.id.temperature_list_item_textview)
        val descriptionTextView:TextView=view.findViewById(R.id.description_list_item_textview)
        val dateTextView:TextView=view.findViewById(R.id.date_list_item_textview)
        val timeTextView:TextView=view.findViewById(R.id.time_list_item_textview)
        val imageView:ImageView=view.findViewById(R.id.image_list_view)
    }
    fun updateData(data:ArrayList<WeatherData>,progressBar: ProgressBar){
        list.clear()
        progressBar.visibility=View.GONE
        list.addAll(data)
        notifyDataSetChanged()
    }
    fun getActivityContext(context: Context){
        this.context=context
    }
}