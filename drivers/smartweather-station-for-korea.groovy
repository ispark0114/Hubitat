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
 *  Based on WooBooung ST DTH, based on original DH codes by SmartThings and SeungCheol Lee(slasher)
 *
 *	2019/04/28 >>> v0.0.15 - Booung     - Updarte reference table
 *	2020/05/22 >>> v0.0.16 - Booung     - Explicit displayed flag
 *  2023/01/22 >>> v2.0.00 - thebearmay - porting to Hubitat
 *  ver. 2.0.1  2023-01-24 - kkossev    - English / 한국인 language option; asynchttpGet(); xml/json response errors handling
 *  ver. 2.0.2  2023-01-25 - kkossev    - AirKorea parsing fixes; code cleanup; 
 */

def version() { "2.0.2" }
def timeStamp() {"2023/01/25 11:29 AM"}

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.XmlSlurper
import groovy.transform.Field

metadata {
	definition (name: "SmartWeather Station For Korea", namespace: "WooBooung", author: "Booung", importUrl: "https://raw.githubusercontent.com/ispark0114/Hubitat/main/drivers/smartweather-station-for-korea.groovy") {
		capability "Sensor"
		capability "Air Quality"                    // airQualityIndex - NUMBER, range:0..500
		capability "Carbon Monoxide Detector"       // co :  ["clear", "tested", "detected"]
		//capability "Dust Sensor"                  // fineDustLevel : PM 2.5   dustLevel : PM 10
        capability "Illuminance Measurement"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
		capability "Ultraviolet Index"              // ultravioletIndex - NUMBER ( WunderGround )
        //capability "Configuration"
		capability "Refresh"
        
        attribute "Info", "string"  
        attribute "fineDustLevel", "number"    // PM 2.5   
        attribute "dustLevel", "number"        // PM 10 airQuality
        attribute "data_time", "string"  

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
        
        command "pollAirKorea"
        //command "pollYuseong"
        //command "pollWunderground"
	}

	preferences {
        input name: "language",	type:"enum", title: "<b>Language / 언어</b>", description: "Select the language / 언어를 선택하세요", defaultValue: "English",options: ["English", "한국인"]
        input (name: "logEnable", type: "bool", title: isEnglish() ? "<b>Debug logging</b>" : "<b>디버그 로깅</b>", description: isEnglish() ? "<i>Debug information, useful for troubleshooting. Recommended value is <b>false</b></i>" : " <i>문제 해결에 유용한 디버그 정보. 권장 값은 <b>false</b></i>입니다." , defaultValue: true)
        input (name: "txtEnable", type: "bool", title: isEnglish() ? "<b>Description text logging</b>" : "<b>설명 텍스트 로깅</b>", description: isEnglish() ? "<i>Display measured values in HE log page. Recommended value is <b>true</b></i>" : "<i>HE 로그 페이지에 측정된 값을 표시합니다. 권장 값은 <b>true</b></i>입니다.", defaultValue: true)
		input "accessKey", "text", type: "password", title: "<b>AirKorea API Key</b>", description: isEnglish() ? "Get an apikey at www.data.go.kr" : "www.data.go.kr에서 apikey 발급 받으세요"  , required: true 
		input "stationName", "text", title: "<b>Station name</b>", description: isEnglish() ? "Station name" : "측청소 이름", required: true
        input "fakeStationName", "text", title: "<b>Fake Station name(option)</b>", description: isEnglish() ? "Enter the name to be displayed on the tile" : "Tile에 보여질 이름 입력하세요", required: false
        input name: "refreshRateMin", title: "<b>Update time in every hour</b>", type: "enum", options:[0 : "0", 15 : "15", 30 : "30"], defaultValue: "15"
        input "coThresholdValue", "decimal", title: "<b>CO Detect Threshold</b>", defaultValue: 0.0, description: isEnglish() ? "When there are more than a few, write down whether to be Detected. default:0.0" : "몇 이상일때 Detected로 할지 적으세요 default:0.0", required: false
        input type: "paragraph", element: "paragraph", title: isEnglish() ? "<b>How to look up measurement stations</b>" : "측정소 조회 방법", description: isEnglish() ? "Enter the desired region through the browser\n http://www.airkorea.or.kr/web/realSearch" : "브라우저 통해 원하시는 지역을 입력하세요\n http://www.airkorea.or.kr/web/realSearch"
		input type: "paragraph", element: "paragraph", title: isEnglish() ? "<b>Source</b>" : "출처", description: isEnglish() ? "Airkorea\nData is real-time observed data and may not be received depending on the local situation at the measurement station or the reception status of the data." : "Airkorea\n데이터는 실시간 관측된 자료이며 측정소 현지 사정이나 데이터의 수신상태에 따라 미수신될 수 있습니다."
   		//input "zipcode", "text", title:"Zip code"
    }
}


private isEnglish() {language=="English"}

// parse events into attributes
def parse(String description) {
	logDebug "Parsing '${description}'"
}

def installed() {
	refresh()
}

def uninstalled() {
	unschedule()
}

def updated() {
	logDebug "updated()"
	refresh()
}

def refresh() {
	logDebug "refresh()"
	unschedule()
    
	def airKoreaHealthCheckInterval = 15

    if ($settings != null && $settings.refreshRateMin != null) {
    	airKoreaHealthCheckInterval = Integer.parseInt($settings.refreshRateMin)
    }

    logDebug "airKoreaHealthCheckInterval $airKoreaHealthCheckInterval"
    
    def wunderGroundHealthCheckInterval = airKoreaHealthCheckInterval + 1
    schedule("0 $airKoreaHealthCheckInterval * * * ?", pollAirKorea)
    //logDebug "wunderGroundHealthCheckInterval $wunderGroundHealthCheckInterval"
    //schedule("0 $wunderGroundHealthCheckInterval * * * ?", pollWunderground)
}

def configure() {
	logDebug "Configuare()"
}

/*
def asyncHttpYuseong(resp, data) {
    def info
    log.trace "data=$data"
    def status = resp.getStatus()
    log.trace "status=$status"
    if (!(status in [200, 207])) {
        info =  "http request error code ${status}"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    def headers = resp.getHeaders()
    log.trace "headers=$headers"
    if( headers == null ) {
        info =  "http request error code ${status} - Missing headers !"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    def respData = resp.getData()
    log.trace "respData length=${respData.length()}"
    log.trace "respData=${respData}"
    if ( respData == null ) {
        info =  "http request error code ${status} - respData is null !"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    // examine the 'Content-Type' - we requested 'json', but errors are returned as 'text/xml'
    def contentType = headers['Content-Type']
    log.trace "Content-Type=${contentType}"
    if (contentType != null && contentType.indexOf('text/xml') >= 0) {
        logDebug "Content-Type=${contentType}"
        def respDataXML = new XmlParser().parseText(respData)
        log.trace "respDataXML = ${respDataXML} "
        def returnReasonCode = respDataXML.cmmMsgHeader.returnReasonCode.text()
        def returnAuthMsg = respDataXML.cmmMsgHeader.returnAuthMsg.text()
        def errMsg = respDataXML.cmmMsgHeader.errMsg.text()
        log.warn "respDataXML.cmmMsgHeader : returnReasonCode=$returnReasonCode, returnAuthMsg=$returnAuthMsg, errMsg=$errMsg"    // returnReasonCode=30, returnAuthMsg=SERVICE_KEY_IS_NOT_REGISTERED_ERROR, errMsg=SERVICE ERROR
        if ((returnReasonCode as int) != 0) {
            info =  "http request returnReasonCode=$returnReasonCode, returnAuthMsg=$returnAuthMsg, errMsg=$errMsg"
            logWarn( info )
            sendEvent(name: "Info", value: info)
            return noResponseData()       
        }
    }
    //def errorData = resp.getErrorData()        // java.lang.Exception: No errorData data exists for async request
    //def errorJson = resp.getErrorJson()	     // java.lang.IllegalArgumentException: No Error JSON exists for response
    //def errorMessage = resp.getErrorMessage()  // java.lang.Exception: No errorMessage exists for async request
    //def errorXml = resp.getErrorXml()          // java.lang.IllegalArgumentException: No Error XML exists for response
    def json = resp.getJson()
    //log.trace "json=$json"
    //def xml = resp.getXml()        // org.xml.sax.SAXParseException: Content is not allowed in prolog.
    def error = resp.hasError()      // error=false
    //log.debug "resp.headers = ${resp.headers}"  // [Transfer-Encoding:chunked, Date:Tue, 24 Jan 2023 21:30:04 GMT, Content-Type:application/json;charset=UTF-8]
    //logWarn "json.response.body.items[0].pm25_value = ${json.response.body.items[0].pm25_value}"
    
    // get the data from the response body
    //logDebug "response data: ${resp.data}"
    if (json.response.body.items == null) {
        logWarn "Missing data (items) !"
        return noResponseData()
    }
    if (json.response.body.items[0].pm10_value != "-") {
        logInfo "PM10 value: ${json.response.body.items[0].pm10_value}"
        sendEvent(name: "pm10_value", value: json.response.body.items[0].pm10_value as Integer, unit: "㎍/㎥")
        sendEvent(name: "dustLevel",  value: json.response.body.items[0].pm10_value as Integer, unit: "㎍/㎥", displayed: true)
    }
    else {
        sendEvent(name: "pm10_value", value: "--", unit: "㎍/㎥")
        sendEvent(name: "dustLevel", value: "--", unit: "㎍/㎥")
    }
    if (json.response.body.items[0].pm25_value != "-") { 
        logInfo "PM25 value: ${json.response.body.items[0].pm25_value}"
        sendEvent(name: "pm25_value", value: json.response.body.items[0].pm25_value as Integer, unit: "㎍/㎥")
        sendEvent(name: "fineDustLevel", value: json.response.body.items[0].pm25_value as Integer, unit: "㎍/㎥", displayed: true)
    }
    else {
        sendEvent(name: "pm25_value", value: "--", unit: "㎍/㎥")
        sendEvent(name: "fineDustLevel", value: "--", unit: "㎍/㎥")
    }
    def display_value
    if (json.response.body.items[0].o3_value != "-") {
       	logInfo "Ozone: ${json.response.body.items[0].o3_value}"
        display_value = "\n" + json.response.body.items[0].o3_value + "\n"
        sendEvent(name: "o3_value", value: display_value as String, unit: "ppm")
    }
    else {
       	sendEvent(name: "o3_value", value: "--", unit: "ppm")
    }
    if (json.response.body.items[0].no2_value != "-") {
        logInfo "NO2: ${json.response.body.items[0].no2_value}"
        display_value = "\n" + json.response.body.items[0].no2_value + "\n"
        sendEvent(name: "no2_value", value: display_value as String, unit: "ppm")
    }
    else {
       	sendEvent(name: "no2_value", value: "--", unit: "ppm")
    }
    if (json.response.body.items[0].so2_value != "-") {
        logInfo "SO2: ${json.response.body.items[0].so2_value}"
        display_value = "\n" + json.response.body.items[0].so2_value + "\n"
        sendEvent(name: "so2_value", value: display_value as String, unit: "ppm")
    }
    else {
       	sendEvent(name: "so2_value", value: "--", unit: "ppm")
    }
    if (json.response.body.items[0].co_value != "-") {
        logInfo "CO: ${json.response.body.items[0].co_value}"
        display_value = "\n" + json.response.body.items[0].co_value + "\n"
        def carbonMonoxide_value = "clear"
        if ((json.response.body.items[0].co_value as Float) >= (coThresholdValue as Float)) {
           	carbonMonoxide_value = "detected"
        }
        sendEvent(name: "carbonMonoxide", value: carbonMonoxide_value, displayed: true)
        sendEvent(name: "co_value", value: display_value as String, unit: "ppm")
    }
    else {
       	sendEvent(name: "co_value", value: "--", unit: "ppm")
    }
    def khai_text = isEnglish() ? "unknown" : "알수없음"
    if (json.response.body.items[0].khai_value != "-") {
        def khai = json.response.body.items[0].khai_value as Integer
        logInfo "Khai value: ${khai}"
        def station_display_name = json.response.body.items[0].station_name
        //     if (fakeStationName) station_display_name = fakeStationName
        logInfo "station_display_name=${station_display_name}, date_time=${json.response.body.items[0].date_time}"
        sendEvent(name:"data_time", value: " " + station_display_name + (isEnglish() ? " air quality numbers: ${khai}\n measurement time: " : " 대기질 수치: ${khai}\n 측정 시간: ") + json.response.body.items[0].date_time)
   		sendEvent(name: "airQualityIndex", value: json.response.body.items[0].khai_value as Integer)
        if (khai > 250) khai_text = isEnglish() ? "very bad" : "매우나쁨"
        else if (khai > 100) khai_text = isEnglish() ? "bad" : "나쁨" 
        else if (khai > 50) khai_text =  isEnglish() ? "normal" : "보통"
        else if (khai >= 0) khai_text =  isEnglish() ? "good" : "좋음"
        sendEvent(name: "airQualityStatus", value: khai_text, unit: "")
    }
    else {
        def station_display_name = json.response.body.items[0].station_name
        //if (fakeStationName) station_display_name = fakeStationName
        sendEvent(name:"data_time", value: " " + station_display_name + (isEnglish() ? " Air quality numbers: no information\n measurement time: " : " 대기질 수치: 정보없음\n 측정 시간: ") + json.response.body.items[0].date_time)                    
        sendEvent(name: "airQualityStatus", value: khai_text)
    }
    sendEvent(name: "Info", value: "Yuseong data parsed succesfuly")
}
*/

def asyncHttpAirKorea(resp, data){
    def info
    //log.trace "data=$data"
    def status = resp.getStatus()
    log.trace "status=$status"
    if (!(status in [200, 207])) {
        info =  "http request error code ${status}"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    def headers = resp.getHeaders()
    log.trace "headers=$headers"
    if( headers == null ) {
        info =  "http request error code ${status} - Missing headers !"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    def respData = resp.getData()
    log.trace "respData length=${respData.length()}"
    if ( respData == null ) {
        info =  "http request error code ${status} - respData is null !"
        logWarn( info )
        sendEvent(name: "Info", value: info)
        return noResponseData()
    }
    // examine the 'Content-Type' - we requested 'json', but errors are returned as 'text/xml'
    def contentType = headers['Content-Type']
    log.trace "Content-Type=${contentType}"
    if (contentType != null && contentType.indexOf('text/xml') >= 0) {
        logDebug "Content-Type=${contentType}"
        def respDataXML = new XmlParser().parseText(respData)
        log.trace "respDataXML = ${respDataXML} "
        def returnReasonCode = respDataXML.cmmMsgHeader.returnReasonCode.text()
        def returnAuthMsg = respDataXML.cmmMsgHeader.returnAuthMsg.text()
        def errMsg = respDataXML.cmmMsgHeader.errMsg.text()
        log.warn "respDataXML.cmmMsgHeader : returnReasonCode=$returnReasonCode, returnAuthMsg=$returnAuthMsg, errMsg=$errMsg"    // returnReasonCode=30, returnAuthMsg=SERVICE_KEY_IS_NOT_REGISTERED_ERROR, errMsg=SERVICE ERROR
        if ((returnReasonCode as int) != 0) {
            info =  "http request returnReasonCode=$returnReasonCode, returnAuthMsg=$returnAuthMsg, errMsg=$errMsg"
            logWarn( info )
            sendEvent(name: "Info", value: info)
            return noResponseData()       
        }
    }
    //def errorData = resp.getErrorData()
    //def errorJson = resp.getErrorJson()	
    //def errorMessage = resp.getErrorMessage()
    //def errorXml = resp.getErrorXml()
    def json = resp.getJson()
    //def xml = resp.getXml()
    def error = resp.hasError()
    //log.trace "xml=$xml error=$error"
    //log.debug "resp.headers = ${resp.headers}"
    
    
// TODO - parse non-existant stations response ( totalCount ) 
// TODO - parse header    {"resultMsg":"NORMAL_CODE","resultCode":"00"}
// http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=%EA%B5%AC%EB%A1%9C%EB%8F%99&dataTerm=DAILY&pageNo=1&numOfRows=1&ServiceKey=JoclFn%2Fvu%2BT%2BESM92PhuHngwYcFJSgOq3o6OzXWj4Ct9Ckj%2FfIApDeTeJEFXVarqMs9BtQYu%2BpEWmfhaXmvNnw%3D%3D&ver=1.3&returnType=json    
// {"response":{"body":{"totalCount":0,"items":[],"pageNo":1,"numOfRows":1},"header":{"resultMsg":"NORMAL_CODE","resultCode":"00"}}}    
    
                    // get the data from the response body
                    logDebug "response data: ${resp.data}"
                    if ( json.response.body.items == null ) {
                        logWarn "Missing data (list) !"
                        return noResponseData()
                    }
                    if ( json.response.body.items[0].pm10Value != "-" ) {
                        logInfo "PM10 value: ${json.response.body.items[0].pm10Value}"
                        sendEvent(name: "pm10_value", value: json.response.body.items[0].pm10Value as Integer, unit: "㎍/㎥")
                        sendEvent(name: "dustLevel", value: json.response.body.items[0].pm10Value as Integer, unit: "㎍/㎥", displayed: true)
                    } else {
                    	sendEvent(name: "pm10_value", value: "--", unit: "㎍/㎥")
                        sendEvent(name: "dustLevel", value: "--", unit: "㎍/㎥")
                    }
                    if ( json.response.body.items[0].pm25Value != "-" ) { 
                        logInfo "PM25 value: ${json.response.body.items[0].pm25Value}"
                        sendEvent(name: "pm25_value", value: json.response.body.items[0].pm25Value as Integer, unit: "㎍/㎥")
                        sendEvent(name: "fineDustLevel", value: json.response.body.items[0].pm25Value as Integer, unit: "㎍/㎥", displayed: true)
                    } else {
                    	sendEvent(name: "pm25_value", value: "--", unit: "㎍/㎥")
                        sendEvent(name: "fineDustLevel", value: "--", unit: "㎍/㎥")
                    }
                    
                    def display_value
                    if ( json.response.body.items[0].o3Value != "-" ) {
                    	logInfo "Ozone: ${json.response.body.items[0].o3Value}"
                        display_value = "\n" + json.response.body.items[0].o3Value + "\n"
                        sendEvent(name: "o3_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "o3_value", value: "--", unit: "ppm")
                    
                    if ( json.response.body.items[0].no2Value != "-" ) {
                        logInfo "NO2: ${json.response.body.items[0].no2Value}"
                        display_value = "\n" + json.response.body.items[0].no2Value + "\n"
                        sendEvent(name: "no2_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "no2_value", value: "--", unit: "ppm")
                    
                    if ( json.response.body.items[0].so2Value != "-" ) {
                        logInfo "SO2: ${json.response.body.items[0].so2Value}"
                        display_value = "\n" + json.response.body.items[0].so2Value + "\n"
                        sendEvent(name: "so2_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "so2_value", value: "--", unit: "ppm")
                    
                    if ( json.response.body.items[0].coValue != "-" ) {
                        logInfo "CO: ${json.response.body.items[0].coValue}"
                        display_value = "\n" + json.response.body.items[0].coValue + "\n"
                        
                        def carbonMonoxide_value = "clear"
                        
                        if ((json.response.body.items[0].coValue as Float) >= (coThresholdValue as Float)) {
                        	carbonMonoxide_value = "detected"
                        }
                        
                        sendEvent(name: "carbonMonoxide", value: carbonMonoxide_value, displayed: true)
                        sendEvent(name: "co_value", value: display_value as String, unit: "ppm")
                    } else
                    	sendEvent(name: "co_value", value: "--", unit: "ppm")
                    
                    def khai_text = isEnglish() ? "unknown" : "알수없음"
                    if ( json.response.body.items[0].khaiValue != "-" ) {
                        def khai = json.response.body.items[0].khaiValue as Integer
                        logInfo "Khai value: ${khai}"
                        
                        def station_display_name = settings?.stationName
                        log.trace "fakeStationName=$fakeStationName, station_display_name=$station_display_name"
                        if (fakeStationName != null) station_display_name = fakeStationName
                        
	                    sendEvent(name:"data_time", value: " " + station_display_name + (isEnglish() ? " air quality: ${khai}\n measurement time: " : " 대기질 수치: ${khai}\n 측정 시간: ") + json.response.body.items[0].dataTime)
                        
                  		sendEvent(name: "airQualityIndex", value: json.response.body.items[0].khaiValue as Integer, displayed: true)

                        if (khai > 250) khai_text = isEnglish() ? "very bad" : "매우나쁨"
                        else if (khai > 100) khai_text = isEnglish() ? "bad" : "나쁨" 
                        else if (khai > 50) khai_text =  isEnglish() ? "normal" : "보통"
                        else if (khai >= 0) khai_text =  isEnglish() ? "good" : "좋음"
                        
                        sendEvent(name: "airQualityStatus", value: khai_text, unit: "")
                        
                    } else {
                        def station_display_name = settings?.stationName
                        if (fakeStationName != null) station_display_name = fakeStationName
	                    sendEvent(name:"data_time", value: " " + station_display_name + (isEnglish() ? " Air quality numbers: no information\n measurement time: " : " 대기질 수치: 정보없음\n 측정 시간: ") + json.response.body.items[0].dataTime)                    
                    	sendEvent(name: "airQualityStatus", value: khai_text)
                    }
    sendEvent(name: "Info", value: "AirKorea data parsed OK")    
}

def noResponseData() {
    def unknown = isEnglish() ? "unknown" : "알수없음"
    
    sendEvent(name: "pm10_value", value: "--", unit: "㎍/㎥")
    sendEvent(name: "dustLevel", value: "--", unit: "㎍/㎥")    
    sendEvent(name: "pm25_value", value: "--", unit: "㎍/㎥")
    sendEvent(name: "fineDustLevel", value: "--", unit: "㎍/㎥")    
    sendEvent(name: "o3_value", value: "--", unit: "ppm")
    sendEvent(name: "no2_value", value: "--", unit: "ppm")
    sendEvent(name: "so2_value", value: "--", unit: "ppm")
    sendEvent(name: "carbonMonoxide", value: "--")
    sendEvent(name: "co_value", value: "--", unit: "ppm")
    sendEvent(name: "airQualityIndex", value: "--")
    sendEvent(name: "airQualityStatus", value: khai_text, unit: "")
    sendEvent(name: "data_time", value: unknown )
    sendEvent(name: "airQualityStatus", value: unknown, unit: "")
    return false
}


// Air Korea handle commands
def pollAirKorea() {
	logDebug "pollAirKorea()"
    def dthVersion = "0.0.11"
    sendEvent(name: "Info", value: "polling AirKorea")
    if (stationName && accessKey) {
        def accessKey_encode = URLEncoder.encode(URLDecoder.decode(settings.accessKey.toString(), "UTF-8"), "UTF-8").replace("+", "%2B");
        //def accessKey_encode = settings.accessKey
        // g5wuVXrLzJMBI9kR2gmdXm6ltsn0zYEicoOG7g2xNHZnGZVp9v7znsIO45M2l7R6rlE5wiD/jtIZupMYvyN2Pg==
        Map params = [
            // https://github.com/mckim27/home-assistant-custom-component/blob/master/custom_components/airkorea/sensor.py  // 
            // Latest commit cf2eeee on Apr 12, 2021
            // http://apis.data.go.kr/B552584
            
    	    uri: "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=${stationName}&dataTerm=DAILY&pageNo=1&numOfRows=1&ServiceKey=${accessKey_encode}&ver=1.3&returnType=json",
        	contentType: 'application/json'//,
            //ignoreSSLIssues: true
    	]
      	logDebug "uri: ${params.uri}"
        asynchttpGet("asyncHttpAirKorea", params)        
        return
	}
    else logDebug "Missing data from the device settings station name or access key"
}

// https://www.yuseong.go.kr/eng/
// https://www.yuseong.go.kr/ys_api/ys_api/getCtprvnList/?pageNo=1&numOfRows=100&rstTy=json

/*
def pollYuseong() {
	logDebug "pollYuseong()"
    sendEvent(name: "Info", value: "polling Yuseong")
    if (stationName) {
        Map params = [
    	    //uri: "https://www.yuseong.go.kr/ys_api/ys_api/getCtprvnList/?pageNo=1&numOfRows=100&rstTy=json",
            //uri: "https://www.yuseong.go.kr/ys_api/ys_api/getCtprvnList/?pageNo=1&numOfRows=10&rstTy=json",            // 10 rows only
            uri: "https://www.yuseong.go.kr/ys_api/ys_api/getCtprvnList/?pageNo=15388&numOfRows=1&rstTy=json",         // 1 row, but last data ?
        	contentType: "application/json",
            ignoreSSLIssues: true
    	]
      	logDebug "uri: ${params.uri}"
        asynchttpGet("asyncHttpYuseong", params)        
        return
	}
    else logWarn "Missing data from the device settings station name"
}
*/

// WunderGround weather handle commands
def pollWunderground() {
	logDebug "pollWunderground()"
	
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
        logInfo "'${obs.sunriseTimeLocal}'"

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
			logWarn "Forecast not found"
		}
	}
	else {
		logWarn "No response from Weather Underground API"
	}
}

// get weather data api
private get() {
	//getTwcConditions(zipCode) // SmartThings WeatherUnderground (WU) APIs specific
}

private localDate(timeZone) {
	def df = new java.text.SimpleDateFormat("yyyy-MM-dd")
	df.setTimeZone(TimeZone.getTimeZone(timeZone))
	df.format(new Date())
}

private send(map) {
	logDebug "WUSTATION: event: $map"
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

def logDebug(msg) {
    if (settings?.logEnable) {
        log.debug "${device.displayName} " + msg
    }
}

def logWarn(msg) {
    if (settings?.logEnable) {
        log.warn "${device.displayName} " + msg
    }
}

def logInfo(msg) {
    if (settings?.txtEnable) {
        log.info "${device.displayName} " + msg
    }
}
