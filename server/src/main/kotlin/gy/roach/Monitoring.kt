package gy.roach

import com.asyncapi.kotlinasyncapi.context.service.AsyncApiExtension
import com.asyncapi.kotlinasyncapi.ktor.AsyncApiPlugin
import com.codahale.metrics.*
import com.sksamuel.cohort.*
import com.sksamuel.cohort.cpu.*
import com.sksamuel.cohort.memory.*
import com.sksamuel.cohort.system.AvailableCoresHealthCheck
import dev.hayden.KHealth
import freemarker.cache.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.metrics.dropwizard.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.html.*
import org.slf4j.event.*

fun Application.configureMonitoring() {
    /*install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(this@configureMonitoring.log)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.SECONDS)
    }*/
    install(KHealth)

    val healthchecks = HealthCheckRegistry(Dispatchers.Default) {
        register(FreememHealthCheck.mb(250), 10.seconds, 10.seconds)
        register(ProcessCpuHealthCheck(0.8), 10.seconds, 10.seconds)
    }

    install(Cohort) {

        // enable an endpoint to display operating system name and version
        operatingSystem = true

        // enable runtime JVM information such as vm options and vendor name
        jvmInfo = true

        // show current system properties
        sysprops = true

        // enable an endpoint to dump the heap in hprof format
        heapDump = true

        // enable an endpoint to dump threads
        threadDump = true

        // set to true to return the detailed status of the healthcheck response
        verboseHealthCheckResponse = true

        // enable healthchecks for kubernetes
        healthcheck("/health", healthchecks)

        // enable healthchecks for kubernetes
        // each of these is optional and can map to any healthcheck url you wish
        // for example if you just want a single health endpoint, you could use /health
        healthcheck("/liveness", healthchecks)
        healthcheck("/readiness", healthchecks)
        healthcheck("/startup", healthchecks)
    }
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

