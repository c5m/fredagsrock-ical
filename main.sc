//> using dep "org.jsoup:jsoup:1.16.1"
//> using dep "org.mnode.ical4j:ical4j:4.0.0-beta7"
//> using dep "com.lihaoyi::requests:0.8.0"

import net.fortuna.ical4j.model.property.TzId
import net.fortuna.ical4j.util.RandomUidGenerator
import net.fortuna.ical4j.model.ComponentList
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar

import java.time.LocalDateTime
import java.io.FileOutputStream

import org.jsoup.Jsoup
import scala.jdk.CollectionConverters._


private def txtToMonth(monthStr: String): Int = {
    monthStr.trim.toLowerCase match
        case "januar"   | "jan" => 1
        case "februar"  | "feb" => 2
        case "marts"    | "mar" => 3
        case "april"    | "apr" => 4
        case "maj"      | "maj" => 5
        case "juni"     | "jun" => 6
        case "juli"     | "jul" => 7
        case "august"   | "aug" => 8
        case "september"| "sep" => 9
        case "oktober"  | "okt" => 10
        case "november" | "nov" => 11
        case "december" | "dec" => 12
        case _ => throw Exception("Unknown month")
}

private def toVEvent(artist: String, dateStr: String): VEvent = {
    val split = dateStr.split("\\.")
    val day = split(0).toInt
    val month = txtToMonth(split(1).trim)
    val year = LocalDateTime.now.getYear()
    val start = LocalDateTime.of(year, month, day, 22, 0)
    val end = start.plusHours(2)
    val ug = new RandomUidGenerator();
    val uid = ug.generateUid();
    
    val event = VEvent(start, `end`, artist)
        .withProperty(TzId("Europe/Copenhagen"))
        .withProperty(ug.generateUid)
        .getFluentTarget[VEvent]()
    event
}

val resp = requests.get("https://www.tivoli.dk/da/kultur-og-program/musik/fredagsrock?json=1")
if  (resp.is2xx){
    val doc = Jsoup.parse(resp.text())
    val links = doc.select("div.table-module__row a[^aria-label]")
    val artistNames = links
    .asScala
    .map(_.attr("aria-label"))
    .toList

    val dates = doc.select("div.table-module__row a span")
    .asScala
    .map(_.text())
    .toList

    if (artistNames.length != dates.length) { println("ERROR: Not same size") }

    val artistsAndDates = artistNames.zip(dates)
    val events = artistsAndDates.map{ case(artist, dateStr) => toVEvent(artist, dateStr) }
    val calendar = Calendar(ComponentList(events.asJava))
        .withDefaults()
        .withProdId("-//Events Calendar//iCal4j 1.0//EN")
        .getFluentTarget()

    val year = LocalDateTime.now.getYear
    val fileOut = new FileOutputStream(s"fredagsrock-$year.ics");
    val outputter = new CalendarOutputter()
    outputter.output(calendar, fileOut)
} else { 
    println(s"ERROR: HTTP response ${resp.statusCode}, ${resp.statusMessage}")
}
