package dev.leonardpark.app.hkoweather

import okhttp3.*
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

interface HKWeatherCurrentListener {
  fun onSuccess(temp: String, humidity: String, cartoon: String)
  fun onFailure(throwable: Throwable)
}

interface HKWeatherWarningListener {
  fun onSuccess(values: ArrayList<String>)
  fun onFailure(throwable: Throwable)
}

class HKWeather {

  companion object {
    private const val current = "http://www.weather.gov.hk/textonly/forecast/englishwx.htm"
    private const val warning = "http://www.weather.gov.hk/textonly/warning/warn.htm"
    private const val nineDay = "http://www.weather.gov.hk/textonly/v2/forecast/nday.htm"

    fun getCurrentWeather(listener: HKWeatherCurrentListener) {
      OkHttpClient().newCall(Request.Builder().url(current).build()).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
          val result = response.body!!.string()

          // 溫度
          var tempResult = ""
          val tempBegin = "Air Temperature : "
          val tempEnd = " degrees Celsius"
          val tempStartIndex = result.indexOf(tempBegin) + tempBegin.length
          val tempEndIndex = result.indexOf(tempEnd, tempStartIndex)
          if (tempStartIndex != -1 && tempEndIndex != -1) {
            val p1 = Pattern.compile("[0-9]+")
            val m1 = p1.matcher(result.substring(tempStartIndex, tempEndIndex))
            while (m1.find()) {
              tempResult = m1.group()
            }
          }

          // 濕度

          // 濕度
          var humidityResult = ""
          val humidityBegin = "Relative Humidity : "
          val humidityEnd = "per cent"
          val humidityStartIndex = result.indexOf(humidityBegin) + humidityBegin.length
          val humidityEndIndex = result.indexOf(humidityEnd, humidityStartIndex)
          if (humidityStartIndex != -1 && humidityEndIndex != -1) {
            val p2 = Pattern.compile("[0-9]+")
            val m2 = p2.matcher(result.substring(humidityStartIndex, humidityEndIndex))
            while (m2.find()) {
              humidityResult = m2.group()
            }
          }

          // 圖示

          // 圖示
          var cartoonResult = ""
          val cartoonBegin = "Weather Cartoon : No. "
          val cartoonEnd = " -"
          val cartoonStartIndex = result.indexOf(cartoonBegin) + cartoonBegin.length
          val cartoonEndIndex = result.indexOf(cartoonEnd, cartoonStartIndex)
          if (cartoonStartIndex != -1 && cartoonEndIndex != -1) {
            val p3 = Pattern.compile("[0-9]+")
            val m3 = p3.matcher(result.substring(cartoonStartIndex, cartoonEndIndex))
            while (m3.find()) {
              cartoonResult = m3.group()
            }
          }

          listener.onSuccess(tempResult, humidityResult, cartoonResult)
        }

        override fun onFailure(call: Call, e: IOException) {
          listener.onFailure(e)
          e.printStackTrace()
        }
      })
    }

    fun getWarningForce(listener: HKWeatherWarningListener) {
      OkHttpClient().newCall(Request.Builder().url(warning).build()).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {

          val result = response.body!!.string()

          val warnNumArray: ArrayList<String> = arrayListOf()

          val warnNum = Vector<String>()

          val warnBegin = "Warning Codes\n"
          val warnEnd = "\n-->"

          val warningStartIndex = result.indexOf(warnBegin) + warnBegin.length
          val warningEndIndex = result.indexOf(warnEnd, warningStartIndex)

          if (warningStartIndex != -1 && warningEndIndex != -1) {
            val p = Pattern.compile("[0-9]+")
            val m = p.matcher(result.substring(warningStartIndex, warningEndIndex))
            while (m.find()) warnNum.add(m.group())
            for (i in warnNum.indices) {
              val warnNo = warnNum[i].toInt()
              warnNumArray.add("" + warnNo)
            }
          }

          listener.onSuccess(warnNumArray)
        }

        override fun onFailure(call: Call, e: IOException) {
          listener.onFailure(e)
          e.printStackTrace()
        }
      })
    }
  }
}