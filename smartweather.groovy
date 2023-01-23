/**
 *	SmartWeather v2022-05-09
 *	clipman@naver.com
 *  날자
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "SmartWeather", namespace: "clipman", author: "clipman") {
		capability 'Sensor'                                 //Sensor
        capability 'Air Quality'						    //airQuality
		//capability 'dustLevel'							//dustLevel, fineDustLevel
		capability 'Temperature Measurement'				//temperature
		capability 'Relative Humidity Measurement'			//humidity
		capability 'Ultraviolet Index'						//ultravioletIndex
		capability 'Illuminance Measurement'				//illuminance
		//capability "circlecircle06391.dustClass"			//dustClass
		//capability "circlecircle06391.fineDustClass"		//fineDustClass
		//capability "circlecircle06391.windspeed"			//windSpeed
		//capability "circlecircle06391.windbearing"			//windBearing
		//capability "circlecircle06391.discomfortIndex"		//discomfortIndex
		//capability "circlecircle06391.discomfortClass"		//discomfortClass
		//capability "circlecircle06391.todayfeeltemp"		//temperatureFeel
		//capability "circlecircle06391.todaymintemp"			//temperatureMin
		//capability "circlecircle06391.todaymaxtemp"			//temperatureMax
		//capability "circlecircle06391.weatherforecast"		//weatherForecast
		//capability "circlecircle06391.ozon"					//ozonLevel
		//capability "circlecircle06391.ozonClass"			//ozonClass
		//capability "circlecircle06391.ultravioletClass"		//ultravioletClass
		//capability "circlecircle06391.locationinfo"			//locationInfo
		//capability "circlecircle06391.precipChance"			//precipChance
		//capability "circlecircle06391.airClass"				//airClass
		//capability "circlecircle06391.pressure"				//pressure
		//capability "circlecircle06391.pressureTrend"		//pressureTrend
		//capability "circlecircle06391.visibility"			//visibility
		//capability "circlecircle06391.sunrise"				//sunrise
		//capability "circlecircle06391.sunset"				//sunset
		//capability "circlecircle06391.moonrise"				//moonrise
		//capability "circlecircle06391.moonset"				//moonset
		//capability "circlecircle06391.moonDay"				//moonDay
		//capability "circlecircle06391.status"				//statusbar
		//capability "circlecircle06391.statusBar"			//status
		//capability "circlecircle06391.station"				//station
		capability "Refresh"

		attribute "weatherIcon", "String"
		attribute "forecastIcon", "String"

        command "pollAirKorea"
		//command "pollWeather"
	}
	preferences {
		input "accessKey", "text", type: "password", title: "AirKorea API Key", required: true
		input "stationName", "text", title: "측청소 이름", defaultValue: "청라", required: true
		//input "subStationName", "text", title: "예비측청소 이름", defaultValue: "중봉", required: true

		//input "status_1", "enum", title: "Select a status1", required: true, options: ["온도", "습도", "미세먼지", "미세먼지등급", "초미세먼지", "초미세먼지등급", "공기질", "날씨", "비올확율", "불쾌지수", "불쾌지수등급", "체감온도", "최저온도", "최고온도", "풍속", "풍향", "기압", "기압변화", "밝기", "시계", "일출", "일몰", "월출", "월몰", "달의일수", "측정소"], defaultValue: "온도"
		//input "status_2", "enum", title: "Select a status2", required: true, options: ["온도", "습도", "미세먼지", "미세먼지등급", "초미세먼지", "초미세먼지등급", "공기질", "날씨", "비올확율", "불쾌지수", "불쾌지수등급", "체감온도", "최저온도", "최고온도", "풍속", "풍향", "기압", "기압변화", "밝기", "시계", "일출", "일몰", "월출", "월몰", "달의일수", "측정소", "표시안함"], defaultValue: "습도"
		//input "status_3", "enum", title: "Select a status3", required: true, options: ["온도", "습도", "미세먼지", "미세먼지등급", "초미세먼지", "초미세먼지등급", "공기질", "날씨", "비올확율", "불쾌지수", "불쾌지수등급", "체감온도", "최저온도", "최고온도", "풍속", "풍향", "기압", "기압변화", "밝기", "시계", "일출", "일몰", "월출", "월몰", "달의일수", "측정소", "표시안함"], defaultValue: "공기질"
		//input "status_4", "enum", title: "Select a status4", required: true, options: ["온도", "습도", "미세먼지", "미세먼지등급", "초미세먼지", "초미세먼지등급", "공기질", "날씨", "비올확율", "불쾌지수", "불쾌지수등급", "체감온도", "최저온도", "최고온도", "풍속", "풍향", "기압", "기압변화", "밝기", "시계", "일출", "일몰", "월출", "월몰", "달의일수", "측정소", "표시안함"], defaultValue: "시계"
		//input "status_5", "enum", title: "Select a status5", required: true, options: ["온도", "습도", "미세먼지", "미세먼지등급", "초미세먼지", "초미세먼지등급", "공기질", "날씨", "비올확율", "불쾌지수", "불쾌지수등급", "체감온도", "최저온도", "최고온도", "풍속", "풍향", "기압", "기압변화", "밝기", "시계", "일출", "일몰", "월출", "월몰", "달의일수", "측정소", "표시안함"], defaultValue: "달의일수"

		input type: "paragraph", element: "paragraph", title: "AirKorea API Key", description: " https://www.data.go.kr/data/15073861/openapi.do<br> 위 사이트에서 활용신청하고 API Key를 발급 받으세요.", displayDuringSetup: false
		input type: "paragraph", element: "paragraph", title: "측정소 조회 방법", description: " 브라우저 통해 원하시는 지역을 검색하세요.<br> http://www.airkorea.or.kr/web/realSearch", displayDuringSetup: false
		//input type: "paragraph", element: "paragraph", title: "만든이", description: "김민수 clipman@naver.com [날자]", displayDuringSetup: false

		//input type: "paragraph", element: "paragraph", title: "HomeAssistant 설정", description: "https://xxx.duckdns.org 또는 http://xxx.duckdns.org:8123", displayDuringSetup: false
		//input "haURL", "text", title: "HomeAssistant URL", required: false
		//input "haToken", "text", title: "HomeAssistant Token", required: false
	}
}

def getUnit(attributes) {
	if(attributes == "온도") return "°C"
	if(attributes == "체감온도") return "°C"
	if(attributes == "최저온도") return "°C"
	if(attributes == "최고온도") return "°C"
	if(attributes == "습도") return "%"
	if(attributes == "비올확율") return "%"
	if(attributes == "미세먼지") return "㎍"
	if(attributes == "초미세먼지") return "㎍"
	if(attributes == "풍속") return "m/s"
	if(attributes == "기압") return "hPa"
	if(attributes == "시계") return "Km"
	if(attributes == "밝기") return "lux"
	return ""
}

def setStatusbar(String status) {
	sendEvent(name: "statusbar", value: status, displayed: false)
}

def installed() {
	refresh()
}

def uninstalled() {
	unschedule()
}

def updated() {
	refresh()
}

def refresh() {
	pollAirKorea()
	//pollWeather()

	unschedule()
	def airKoreaHealthCheckInterval = 15
	schedule("0 $airKoreaHealthCheckInterval * * * ?", pollAirKorea)

	//runEvery10Minutes(pollWeather)
}

def pollAirKorea() {
	airKorea(settings.stationName)
	if(settings.subStationName && device.currentValue("dustLevel") == 0 && device.currentValue("fineDustLevel") == 0) {
		airKorea(settings.subStationName)
	}
}

def airKorea(stationName) {
	//log.debug "pollAirKorea()"
	if (stationName && settings.accessKey) {
		def accessKey_encode = URLEncoder.encode(URLDecoder.decode(settings.accessKey.toString(), "UTF-8"), "UTF-8").replace("+", "%2B");
		def params = [
			uri: "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=${stationName}&dataTerm=DAILY&pageNo=1&numOfRows=1&ServiceKey=${accessKey_encode}&ver=1.3&returnType=json",
			contentType: 'application/json'
		]

		def currentStatus = ""
		try {
			httpGet(params) {resp ->
				resp.headers.each {
					//log.debug "${it.name} : ${it.value}"
				}
				if (resp.status == 200) {
					//log.debug "response data: ${resp.data}"
					if(resp.data.response.body.items[0].pm10Flag != null) {
						currentStatus = resp.data.response.body.items[0].pm10Flag + ", 측정 시간: " + resp.data.response.body.items[0].dataTime
					}

					if( resp.data.response.body.items[0].pm10Value != "-" ) {
						def dustLevel = resp.data.response.body.items[0].pm10Value as Integer
						def dustClass
						if (dustLevel > 150) dustClass = "최악"
						else if (dustLevel > 80) dustClass = "나쁨"
						else if (dustLevel > 30) dustClass = "보통"
						else if (dustLevel >= 0) dustClass = "좋음"

						sendEvent(name: "dustLevel", value: dustLevel, unit: "㎍/㎥")
						sendEvent(name: "dustClass", value: dustClass)
					} else {
						sendEvent(name: "dustLevel", value: 0, unit: "㎍/㎥")
						sendEvent(name: "dustClass", value: "모름")
					}

					if( resp.data.response.body.items[0].pm25Value != "-" ) {
						def fineDustLevel = resp.data.response.body.items[0].pm25Value as Integer
						def fineDustClass
						if (fineDustLevel > 75) fineDustClass = "최악"
						else if (fineDustLevel > 35) fineDustClass = "나쁨"
						else if (fineDustLevel > 15) fineDustClass = "보통"
						else if (fineDustLevel >= 0) fineDustClass = "좋음"

						sendEvent(name: "fineDustLevel", value: fineDustLevel, unit: "㎍/㎥")
						sendEvent(name: "fineDustClass", value: fineDustClass)
					} else {
						sendEvent(name: "fineDustLevel", value: 0, unit: "㎍/㎥")
						sendEvent(name: "fineDustClass", value: "모름")
					}

					if( resp.data.response.body.items[0].khaiValue != "-" ) {
						def airQuality = resp.data.response.body.items[0].khaiValue as Integer
						def airClass
						if (airQuality > 250) airClass = "최악"
						else if (airQuality > 100) airClass = "나쁨"
						else if (airQuality > 50) airClass = "보통"
						else if (airQuality >= 0) airClass = "좋음"

						sendEvent(name: "airQuality", value: airQuality)
						sendEvent(name: "airClass", value: airClass)
					} else {
						sendEvent(name: "airQuality", value: 0)
						sendEvent(name: "airClass", value: "모름")
					}

					if( resp.data.response.body.items[0].o3Value != "-" ) {
						def ozonLevel = resp.data.response.body.items[0].o3Value as float
						def ozonClass
						if (ozonLevel > 0.15) ozonClass = "최악"
						else if (ozonLevel > 0.1) ozonClass = "나쁨"
						else if (ozonLevel > 0.03) ozonClass = "보통"
						else if (ozonLevel >= 0) ozonClass = "좋음"

						sendEvent(name: "ozonLevel", value: ozonLevel, unit: "ppm")
						sendEvent(name: "ozonClass", value: ozonClass)
					} else {
						sendEvent(name: "ozonLevel", value: 0, unit: "ppm")
						sendEvent(name: "ozonClass", value: "모름")
					}

					sendEvent(name: "status", value: stationName + ", 측정 시간: " + resp.data.response.body.items[0].dataTime)
				} else if (resp.status == 429) {
					sendEvent(name: "status", value: stationName + ", 최대조회수 초과")
				} else if (resp.status == 500) {
					sendEvent(name: "status", value: stationName + ", 서버에러")
				} else {
					sendEvent(name: "status", value: stationName + ", Error: " + resp.status)
				}
			}
		} catch (e) {
			sendEvent(name: "ozonLevel", value: 0, unit: "ppm")
			sendEvent(name: "ozonClass", value: "모름")
			sendEvent(name: "status", value: stationName + ", " + currentStatus)
			log.debug "uri: ${params.uri}"
		}
	}
	else {
		//log.debug "Missing data from the device settings station name or access key"
	}
	sendEvent(name: "station", value: stationName, displayed: false)
	
}

private convertWindSpeed(value, fromScale, toScale) {
	def fs = fixScale(fromScale)
	def ts = fixScale(toScale)
	if (fs == ts) {
		return value
	}
	if (ts == 'imperial') {
		return value / 1.609
	}
	return value * 1.609
}

private createCityName(location) {
	def cityName = null

	if (location) {
		cityName = location.city + ", "

		if (location.adminDistrictCode) {
			cityName += location.adminDistrictCode
			cityName += " "
			cityName += location.countryCode ?: location.country
		} else {
			cityName += location.country
		}
	}
	return cityName
}

private fixScale(scale) {
	switch (scale.toLowerCase()) {
	case "c":
	case "metric":
		return "metric"
	default:
		return "imperial"
	}
}

def publishDevice() {
	def data = [:]
	data["name"] = device.name
	data["airQuality"] = device.currentValue("airQuality")
	data["dustLevel"] = device.currentValue("dustLevel")
	data["fineDustLevel"] = device.currentValue("fineDustLevel")
	data["temperature"] = device.currentValue("temperature")
	data["humidity"] = device.currentValue("humidity")
	data["ultravioletIndex"] = device.currentValue("ultravioletIndex")
	data["illuminance"] = device.currentValue("illuminance")
	data["dustClass"] = device.currentValue("dustClass")
	data["fineDustClass"] = device.currentValue("fineDustClass")
	data["windSpeed"] = device.currentValue("windSpeed")
	data["windBearing"] = device.currentValue("windBearing")
	data["discomfortIndex"] = device.currentValue("discomfortIndex")
	data["discomfortClass"] = device.currentValue("discomfortClass")
	data["temperatureFeel"] = device.currentValue("temperatureFeel")
	data["temperatureMin"] = device.currentValue("temperatureMin")
	data["temperatureMax"] = device.currentValue("temperatureMax")
	data["weatherForecast"] = device.currentValue("weatherForecast")
	data["ozonLevel"] = device.currentValue("ozonLevel")
	data["ozonClass"] = device.currentValue("ozonClass")
	data["ultravioletClass"] = device.currentValue("ultravioletClass")
	data["locationInfo"] = device.currentValue("locationInfo")
	data["precipChance"] = device.currentValue("precipChance")
	data["airClass"] = device.currentValue("airClass")
	data["pressure"] = device.currentValue("pressure")
	data["pressureTrend"] = device.currentValue("pressureTrend")
	data["visibility"] = device.currentValue("visibility")
	data["sunrise"] = device.currentValue("sunrise")
	data["sunset"] = device.currentValue("sunset")
	data["moonrise"] = device.currentValue("moonrise")
	data["moonset"] = device.currentValue("moonset")
	data["moonDay"] = device.currentValue("moonDay")
	data["statusbar"] = device.currentValue("statusbar")
	data["status"] = device.currentValue("status")
	data["station"] = device.currentValue("station")

	data["weatherIcon"] = device.currentValue("weatherIcon")
	data["forecastIcon"] = device.currentValue("forecastIcon")

	data["update"] = new Date().format("yyyy-MM-dd HH:mm:ss", location.timeZone)

	def payload = new groovy.json.JsonOutput().toJson(data)
	services("/api/services/mqtt/publish", ["topic": "smartthings/"+device.deviceNetworkId, "payload": "'" + payload.toString() + "'"])
	log.debug "publishDevice: ${device.deviceNetworkId}/, " + payload.toString()
}