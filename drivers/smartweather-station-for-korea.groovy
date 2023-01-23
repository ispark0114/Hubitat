/**
 *  SmartWeather Station For Korea
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Based on original DH codes by SmartThings and SeungCheol Lee(slasher)
 */
public static String version() { return "v2.0.00.20230122" }
/*
 *  2023/01/22 >>> v2.0.00 - thebearmay - porting to Hubitat
 *	2020/05/22 >>> v0.0.16 - Booung     - Explicit displayed flag
 *	2019/04/28 >>> v0.0.15 - Booung     - Updarte reference table
 */
  
metadata {
	definition (name: "SmartWeather Station For Korea", namespace: "WooBooung", author: "Booung", ocfDeviceType: "x.com.st.d.airqualitysensor") {
		capability "Air Quality"// Sensor"
		capability "Carbon Monoxide Detector" // co : clear, detected
		//capability "Dust Sensor" // fineDustLevel : PM 2.5   dustLevel : PM 10
        capability "Illuminance Measurement"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
		capability "Ultraviolet Index"
		capability "Polling"
        capability "Configuration"
		capability "Refresh"
		capability "Sensor"

		// Air Korea infos for WebCore
		attribute "airQualityStatus", "string"
		attribute "pm25_value", "number"
        attribute "pm10_value", "number"
        attribute "o3_value", "number"
		attribute "no2_value", "number"
		attribute "so2_value", "number"
        attribute "co_value", "number"
        
        // Weather Station infos
        attribute "localSunrise", "string"
		attribute "localSunset", "string"
        attribute "city", "string"
		attribute "timeZoneOffset", "string"
		attribute "weather", "string"
		attribute "wind", "number"
		attribute "weatherIcon", "string"
		attribute "forecastIcon", "string"
		attribute "feelsLike", "number"
		attribute "percentPrecip", "number"
        
        command "refresh"
        command "pollAirKorea"
        command "pollWunderground"
	}

	preferences {
		input "accessKey", "text", type: "password", title: "AirKorea API Key", description: "www.data.go.kr에서 apikey 발급 받으세요", required: true 
		input "stationName", "text", title: "Station name", description: "측청소 이름", required: true
        input "fakeStationName", "text", title: "Fake Station name(option)", description: "Tile에 보여질 이름 입력하세요", required: false
        input name: "refreshRateMin", title: "Update time in every hour", type: "enum", options:[0 : "0", 15 : "15", 30 : "30"], defaultValue: "15", displayDuringSetup: true
        input "coThresholdValue", "decimal", title: "CO Detect Threshold", defaultValue: 0.0, description: "몇 이상일때 Detected로 할지 적으세요 default:0.0", required: false
        input type: "paragraph", element: "paragraph", title: "측정소 조회 방법", description: "브라우저 통해 원하시는 지역을 입력하세요\n http://www.airkorea.or.kr/web/realSearch", displayDuringSetup: false
		input type: "paragraph", element: "paragraph", title: "출처", description: "Airkorea\n데이터는 실시간 관측된 자료이며 측정소 현지 사정이나 데이터의 수신상태에 따라 미수신될 수 있습니다.", displayDuringSetup: false
        input type: "paragraph", element: "paragraph", title: "Version", description: version(), displayDuringSetup: false
   		input "zipcode", "text", title:"Zip code"
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def installed() {
	refresh()
}

def uninstalled() {
	unschedule()
}

def updated() {
	log.debug "updated()"
	refresh()
}

def refresh() {
	log.debug "refresh()"
	unschedule()
    
	def airKoreaHealthCheckInterval = 15

    if ($settings != null && $settings.refreshRateMin != null) {
    	airKoreaHealthCheckInterval = Integer.parseInt($settings.refreshRateMin)
    }

    log.debug "airKoreaHealthCheckInterval $airKoreaHealthCheckInterval"
    
    def wunderGroundHealthCheckInterval = airKoreaHealthCheckInterval + 1
    schedule("0 $airKoreaHealthCheckInterval * * * ?", pollAirKorea)
    log.debug "wunderGroundHealthCheckInterval $wunderGroundHealthCheckInterval"
    schedule("0 $wunderGroundHealthCheckInterval * * * ?", pollWunderground)
}

def configure() {
	log.debug "Configuare()"
}

// Air Korea handle commands
def pollAirKorea() {
	log.debug "pollAirKorea()"
    def dthVersion = "0.0.11"
    if (stationName && accessKey) {
        def params = [
    	    uri: "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=${stationName}&dataTerm=DAILY&pageNo=1&numOfRows=1&ServiceKey=${accessKey}&ver=1.3&returnType=json",
        	contentType: 'application/json'
    	]
        
        try {
        	log.debug "uri: ${params.uri}"
            
            httpGet(params) {resp ->
                resp.headers.each {
                    log.debug "${it.name} : ${it.value}"
                }
                // get the contentType of the response
                log.debug "response contentType: ${resp.contentType}"
                // get the status code of the response
                log.debug "response status code: ${resp.status}"
                if (resp.status == 200) {
                    // get the data from the response body
                    //log.debug "response data: ${resp.data}"
              
                    if( resp.data.list[0].pm10Value != "-" ) {
                        log.debug "PM10 value: ${resp.data.list[0].pm10Value}"
                        sendEvent(name: "pm10_value", value: resp.data.list[0].pm10Value as Integer, unit: "㎍/㎥")
                        sendEvent(name: "dustLevel", value: resp.data.list[0].pm10Value as Integer, unit: "㎍/㎥", displayed: true)
                    } else {
                    	sendEvent(name: "pm10_value", value: "--", unit: "㎍/㎥")
                        sendEvent(name: "dustLevel", value: "--", unit: "㎍/㎥")
                    }
                    
                    if( resp.data.list[0].pm25Value != "-" ) { 
                        log.debug "PM25 value: ${resp.data.list[0].pm25Value}"
                        sendEvent(name: "pm25_value", value: resp.data.list[0].pm25Value as Integer, unit: "㎍/㎥")
                        sendEvent(name: "fineDustLevel", value: resp.data.list[0].pm25Value as Integer, unit: "㎍/㎥", displayed: true)
                    } else {
                    	sendEvent(name: "pm25_value", value: "--", unit: "㎍/㎥")
                        sendEvent(name: "fineDustLevel", value: "--", unit: "㎍/㎥")
                    }
                    
                    def display_value
                    if( resp.data.list[0].o3Value != "-" ) {
                    	log.debug "Ozone: ${resp.data.list[0].o3Value}"
                        display_value = "\n" + resp.data.list[0].o3Value + "\n"
                        sendEvent(name: "o3_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "o3_value", value: "--", unit: "ppm")
                    
                    if( resp.data.list[0].no2Value != "-" ) {
                        log.debug "NO2: ${resp.data.list[0].no2Value}"
                        display_value = "\n" + resp.data.list[0].no2Value + "\n"
                        sendEvent(name: "no2_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "no2_value", value: "--", unit: "ppm")
                    
                    if( resp.data.list[0].so2Value != "-" ) {
                        log.debug "SO2: ${resp.data.list[0].so2Value}"
                        display_value = "\n" + resp.data.list[0].so2Value + "\n"
                        sendEvent(name: "so2_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "so2_value", value: "--", unit: "ppm")
                    
                    if( resp.data.list[0].coValue != "-" ) {
                        log.debug "CO: ${resp.data.list[0].coValue}"
                        display_value = "\n" + resp.data.list[0].coValue + "\n"
                        
                        def carbonMonoxide_value = "clear"
                        
                        if ((resp.data.list[0].coValue as Float) >= (coThresholdValue as Float)) {
                        	carbonMonoxide_value = "detected"
                        }
                        
                        sendEvent(name: "carbonMonoxide", value: carbonMonoxide_value, displayed: true)
                        sendEvent(name: "co_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "co_value", value: "--", unit: "ppm")
                    
                    def khai_text = "알수없음"
                    if( resp.data.list[0].khaiValue != "-" ) {
                        def khai = resp.data.list[0].khaiValue as Integer
                        log.debug "Khai value: ${khai}"
                        
                        def station_display_name = resp.data.parm.stationName
                        
                        if (fakeStationName)
                        	station_display_name = fakeStationName
                        
	                    sendEvent(name:"data_time", value: " " + station_display_name + " 대기질 수치: ${khai}\n 측정 시간: " + resp.data.list[0].dataTime + "\nVersion: " + dthVersion)
                        
                  		sendEvent(name: "airQuality", value: resp.data.list[0].khaiValue as Integer, displayed: true)

                        if (khai > 250) khai_text="매우나쁨"
                        else if (khai > 100) khai_text="나쁨"
                        else if (khai > 50) khai_text="보통"
                        else if (khai >= 0) khai_text="좋음"
                        
                        sendEvent(name: "airQualityStatus", value: khai_text, unit: "")
                        
                    } else {
                        def station_display_name = resp.data.parm.stationName
                        
                        if (fakeStationName)
                        	station_display_name = fakeStationName

                    
	                    sendEvent(name:"data_time", value: " " + station_display_name + " 대기질 수치: 정보없음\n 측정 시간: " + resp.data.list[0].dataTime)                    
                    	sendEvent(name: "airQualityStatus", value: khai_text)
                    }
          		}
            	else if (resp.status==429) log.debug "You have exceeded the maximum number of refreshes today"	
                else if (resp.status==500) log.debug "Internal server error"
            }
        } catch (e) {
            log.error "error: $e"
        }
	}
    else log.debug "Missing data from the device settings station name or access key"
}

// WunderGround weather handle commands
def pollWunderground() {
	log.debug "pollAirKorea()"
	
	// Current conditions

    def obs = get()
	if (obs) {
		//def weatherIcon = obs.icon_url.split("/")[-1].split("\\.")[0]

		if(getTemperatureScale() == "C") {
			send(name: "temperature", value: Math.round(obs.temperature), unit: "C", displayed: true)
			send(name: "feelsLike", value: Math.round(obs.temperatureFeelsLike as Double), unit: "C")            
		} else {
			send(name: "temperature", value: Math.round(obs.temperature), unit: "F", displayed: true)
			send(name: "feelsLike", value: Math.round(obs.temperatureFeelsLike as Double), unit: "F") 
		}
		
        send(name: "humidity", value: obs.relativeHumidity as Integer, unit: "%", displayed: true)
        send(name: "weather", value: obs.wxPhraseShort)
        send(name: "weatherIcon", value: obs.iconCode as String, displayed: false)
        send(name: "wind", value: Math.round(obs.windSpeed) as Integer, unit: "MPH")

		//loc
		def loc = getTwcLocation(zipCode).location
        
        //timezone
        def localTimeOffSet = "+" + obs.validTimeLocal.split("\\+")[1]
        
		if (localTimeOffSet != device.currentValue("timeZoneOffset")) {
            send(name: "timeZoneOffset", value: localTimeOffSet)
		}
        
        def cityValue = "${loc.city}, ${loc.adminDistrict}, ${loc.countryCode}"
		if (cityValue != device.currentValue("city")) {
            send(name: "city", value: cityValue)
		}

        send(name: "ultravioletIndex", value: Math.round(obs.uvIndex as Double))

		// Sunrise / Sunset
        def dtf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        def sunriseDate = dtf.parse(obs.sunriseTimeLocal)
        log.info "'${obs.sunriseTimeLocal}'"

        def sunsetDate = dtf.parse(obs.sunsetTimeLocal)

        def tf = new java.text.SimpleDateFormat("h:mm a")
        tf.setTimeZone(TimeZone.getTimeZone(loc.ianaTimeZone))

        def localSunrise = "${tf.format(sunriseDate)}"
        def localSunset = "${tf.format(sunsetDate)}"
        
        send(name: "localSunrise", value: localSunrise, descriptionText: "Sunrise today is at $localSunrise")
        send(name: "localSunset", value: localSunset, descriptionText: "Sunset today at is $localSunset")

        send(name: "illuminance", value: estimateLux(obs, sunriseDate, sunsetDate), displayed: true)

		// Forecast
        def f = getTwcForecast(zipCode)
         if (f) {
            def icon = f.daypart[0].iconCode[0] ?: f.daypart[0].iconCode[1]
            def value = f.daypart[0].precipChance[0] as Integer ?: f.daypart[0].precipChance[1] as Integer
            def narrative = f.daypart[0].narrative
            send(name: "percentPrecip", value: value, unit: "%")
            send(name: "forecastIcon", value: icon, displayed: false)
        }       
		else {
			log.warn "Forecast not found"
		}
	}
	else {
		log.warn "No response from Weather Underground API"
	}
}

// get weather data api
private get() {
	getTwcConditions(zipCode)
}

private localDate(timeZone) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	df.setTimeZone(TimeZone.getTimeZone(timeZone))
	df.format(new Date())
}

private send(map) {
	log.debug "WUSTATION: event: $map"
	sendEvent(map)
}

private estimateLux(obs, sunriseDate, sunsetDate) {
	def lux = 0
	def now = new Date().time

    if(obs.dayOrNight != 'N') {
		//day
        switch(obs.iconCode) {
            case '04':
                lux = 200
                break
            case ['05', '06', '07', '08', '09', '10',
                  '11', '12', '13','14', '15','17','18','19','20',
                  '21','22','23','24','25','26']:
                lux = 1000
                break
            case ['27', '28']:
                lux = 2500
                break
            case ['29', '30']:
                lux = 7500
                break
            default:
                //sunny, clear
                lux = 10000
		}

		//adjust for dusk/dawn
		def afterSunrise = now - sunriseDate.time
		def beforeSunset = sunsetDate.time - now
		def oneHour = 1000 * 60 * 60

		if(afterSunrise < oneHour) {
			//dawn
			lux = (long)(lux * (afterSunrise/oneHour))
		} else if (beforeSunset < oneHour) {
			//dusk
			lux = (long)(lux * (beforeSunset/oneHour))
		}
	} else {
		//night - always set to 10 for now
		//could do calculations for dusk/dawn too
		lux = 10
	}

	lux
}
