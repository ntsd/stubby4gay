#### 6.0.3-SNAPSHOT

#### 6.0.2

* Pull request #105 - Update org.yaml:snakeyaml:1.23, which resolves issue #100 (https://github.com/moleksyuk)

#### 6.0.1

* Pull request #93 - Added support for stubbed `PATCH` requests (https://github.com/singh-virendra)

#### 6.0.0

* Pull request #87 - Added support for UUID property & ability to get/delete/update stub by UUID (https://github.com/ntsd)
* Displaying UUID value in popup dialogs in Admin console when `[view]` YAML source button is clicked
* Revisited HTTP response codes of Admin portal APIs that manage stubs (delete, get & update APIs)
* Upgraded from Jetty `9.4.8.v20171121` to `9.4.9.v20180320`
* Moved from `XMLUnit v1.x` to `XMLUnit v2.x` for a more optimized XML evaluation
* Using `ehCache` for internal caching
- Upon parsing of YAML config, Compiling regex patterns using different flag combinations: 
  - `Pattern.MULTILINE`
  - `Pattern.MULTILINE | Pattern.DOTALL`
  - `Pattern.LITERAL` (as a fallback if any of the aforementioned compilations failed)

#### 5.2.0
* Pull request #91 - Added ability to use tokenized "Location" header in 3xx responses (https://github.com/dimadl)
* Issue #92 - Added ability to honor stubbed 404 responses (https://github.com/MannanM)

#### 5.1.1
* Added ANSITerminal back. it is operational alongside the SLF4J for the cases when stubby is running as a standalone jar
* Upgraded from Jetty `9.4.6.v20170531` to `9.4.8.v20171121`

#### 5.1.0
* Pull request #83 - ANSITerminal was replaced with SLF4J. It is up to the stuby4j consumer to choose their own logging implementation (https://github.com/asarkar)

#### 5.0.2
* Pull request #77 - New field `description` for stubs/features (https://github.com/goughy000)

#### 5.0.1
* Pull request #71 - Add endpoint to Delete all stubs (https://github.com/nningego)
* Pull request #74 - Adding parsing tokens capabilities to the file body (https://github.com/OtavioRMachado)
* Pull request #76 - Allow custom XML and JSON content types (https://github.com/goughy000)
* Upgraded from Jetty `9.4.0.v20161208` to `9.4.6.v20170531`

#### 5.0.0
* 2017 release: a lot of internal maintenance such as code clean up, refactoring & improved test coverage
* Supporting additional 3xx redirect HTTP codes when rendering redirect response: `303`, `307` & `308`

#### 4.0.5
* Pull request #63 - Dynamic token replacement is also applied to stubbed response headers
* Upgraded from Jetty `9.3.13.v20161014` to `9.4.0.v20161208`
* Added dependency on https://github.com/ntsd/collection-type-safe-converter
* 'Builder' sub-project got merged into the 'Main' sub-project

#### 4.0.4
* Upgraded from Jetty `9.3.12.v20160915` to `9.3.13.v20161014`
* Shaved off stubby's start-up time due to parsing YAML config asynchronously
* Issue #61 - During record & play, the stubbed query params were sent with recording request instead of the actual request query params

#### 4.0.3
* Optimized the stub matching algorithm by caching the previous matches [StubRepository#matchStub](https://github.com/ntsd/stubby4gay/blob/master/main/java/io/github/ntsd/stubby4gay/database/StubRepository.java)
* Suppressed Jetty's default [ErrorHandler](http://download.eclipse.org/jetty/9.3.12.v20160915/apidocs/org/eclipse/jetty/server/handler/ErrorHandler.html) with a custom [JsonErrorHandler](main/java/io/github/ntsd/stubby4gay/handlers/JsonErrorHandler.java) to send errors in JSON format
* Got rid off repackaged classes from Apache Commons in favor of Java 8 APIs
* Using Java NIO for file operations

#### 4.0.2
* Log to terminal why a request fails to match https://github.com/soundcloud/stubby4gay/commit/5901710efd31653a05804ebec62f67184c212832
* Square brackets were not escaped as literals for regular expression in Json POST [BUG]
* Pre-compiling & caching stubbed regex patterns upon parsing YAML stub configuration

#### 4.0.1
* Issue #54 - Support for regular expression in Json POST

#### 4.0.0
* Built using Java v1.8 (`1.8.0_60`)
* Updated Gradle `build.gradle` to compile using Java v1.8
* Upgraded from Jetty `9.2.10.v20150310` to `9.3.12.v20160915`
* Updated Docker config
* Renamed project root package from `by.stub` to `io.github.ntsd.stubby4gay`
* Renamed Maven Central group ID from `by.stub` to `io.github.ntsd`
* Issue #55 - When running in `--debug`, dumping `HttpServletRequest` parameters, would implicitly call `ServletRequest#getInputStream()`
* Issue #56 - Requests with query parameters values containing white spaces
* Pull request #57 - `StubbyClient` starts Jetty with `-m` to mute the console logger, but it wasn't actually muted 

#### 3.3.0
* Allow callers to wait for the StubbyClient to finish (Oliver Weiler, https://github.com/helpermethod)
* Serving response files from a local path constructed with regex tokens from the stubbed request (Radek Ostrowski, https://github.com/radek1st)

#### 3.2.3
* Dumping more debug information to the console if `--debug` option is on, also for successfully matched requests
* Added support for `PUT` and `DELETE` methods in `StubbyClient` class

#### 3.1.3
* If POST'ed data type is `application/json`, the comparison of stubbed to posted data will be done using JSON entities with non-strict checking (content ordering wont matter, as long as it is the same)
* If POST'ed data type is `application/xml`, the comparison of stubbed to posted data will be done using XML entities with non-strict checking (element & attribute ordering wont matter, as long as content is the same)

#### 3.0.3
* Added support for custom authorization type header with the help of the new `header` property `authorization-custom`
* Fixed issue #43 (Live refresh in response sequence only for first response)

#### 3.0.2
* Added support for Bearer Token authorization with the help of the new `header` property `authorization-bearer`
* Renamed existing `header` property `authorization` to `authorization-basic`
* Some changes around the programmatic APIs in `StubbyClient` class due to the above changes
* Respective changes in the current README due to the above changes

#### 3.0.1
* Upgraded Jetty to v9.2.10.v20150310
* Added `--debug` option that dumps incoming raw HTTP request to the console
* Added `--disable_admin_portal` option that does not configure Admin portal for stubby
* Added `--disable_ssl` option that does not enable SSL for stubby
* Added a new API to start stubby programmatically without specifying a YAML file `StubbyClient.startJettyYamless(...)`
* Added a new `FaviconHandler` to handle requests for `favicon.ico` under the context root

#### 3.0.0
* Built using Java v1.7.0_76
* Updated Jetty to v9.2.9.v20150224 (requires at least JRE v1.7.0_76: [Issue with Java v1.7.0_04](http://dev.eclipse.org/mhonarc/lists/jetty-users/msg05635.html))

#### 2.0.22
* Built using Java v1.7.0_04
* Cleaned up project Gradle configuration 
* Updated Gradle configuration to be compatible with Gradle v2.2.1 & Gradle Nexus plugin v2.3
* Updated all (except Jetty) dependencies to their latest versions (as of May 10th, 2015)

#### 2.0.21
* Added console outputs for record & play functionality

#### 2.0.20
* Replacing all hardcoded `\n` with dynamically generated system line break characters

#### 2.0.19
* Record&Play is now more intelligent: when stubbed `request` is matched, its stubbed properties (`method`, `url`, `headers`, `post` and `query`) are used to construct HTTP request to the recordable destination URL provided in stubbed `response` body [ENHANCEMENT]
* Added a workaround a limitation in SnakeYAML v1.13 used by stubby (it has limited JSON support, not all the JSON documents can be parsed) where it cannot parse escaped forward slashes in JSON [BUG]
* Refreshing Admin status page was changing sequenced response counter ID [BUG]
* Replaced hardcoded Unix new line character '\n' in YamlBuilderTest that caused the tests to fail on Windows [BUG]
* Admin status page now shows what is the next sequenced response in the sequence queue [ENHANCEMENT]
* Supporting HTTP requests with empty query params: `/uri?some_param=` or `/uri?some_param` [ENHANCEMENT]

#### 2.0.18
* When `--data` file was just a relative filename without parent directory, NPE was thrown when Admin portal status page was loaded [BUG]
* Configured jetty GZIP handler for static resources and Stubs & Admin portals [ENHANCEMENT]
* Added resource hit stats section to Admin portal status page [ENHANCEMENT]

#### 2.0.17
* Added a verification check in StubbyManager to make sure that Jetty has been started or shut down [ENHANCEMENT]
* Upgraded SnakeYAML dependency library to v1.13 [ENHANCEMENT]
* Admin status page: displaying YAML snippet for a given `request` or `response` separately instead of pairing them up [ENHANCEMENT]
* Admin status page: displaying metadata of loaded external files (if any) [ENHANCEMENT]
* Admin status page: displaying Ajax response in a JS popup, instead of injecting the Ajax response into HTML table [ENHANCEMENT]
* A bunch of cosmetic changes on Admin status page [COSMETICS]

#### 2.0.16
* Displaying stubby JAR: version, its classpath location, built date, up-time, its input args and Java input args on status page [ENHANCEMENT]
* Displaying stubby heap/non-heap memory usage on status page [ENHANCEMENT]
* Added an option on admin status page to display YAML snippet for a given request & response pair [ENHANCEMENT]
* Making sure that 'x-stubby-resource-id' header is always the first item in the stubbed headers on status page [ENHANCEMENT]
* Changed colors of status page [COSMETICS]

#### 2.0.15
* When creating template token names for `query` or `headers` regex matches, the name format to be followed should be `headers.key_name.ID` or `query.key_name.ID` [ENHANCEMENT]

#### 2.0.14
* Whitespace was not allowed between the `<% ` & ` %>` and what's inside when specifying template tokens for dynamic token replacement in stubbed response [BUG]
* Regex matches were stored against incorrect token names for `query` and `headers` regexes [BUG]
* Renamed command line arg `--ssl` to `--tls` to reduce the confusion when having another command line arg that starts with letter `s`, like `--stubs` [ENHANCEMENT]
* Added command line arg `--version` that prints current stubby4gay version to the console [ENHANCEMENT]

#### 2.0.13
* Dynamic token replacement in stubbed response, by leveraging regex capturing groups as token values during HTTP request verification [FEATURE]

#### 2.0.12
* Removed flag `--watch_sleep_time`. The `--watch` flag can now accept an optional arg value which is the watch scan time in milliseconds. If milliseconds is not provided, the watch scans every 100ms [ENHANCEMENT]
* Added additional API to start Jetty via StubbyClient by specifying an address to bind [ENHANCEMENT]

#### 2.0.11
* `--watch` flag sleep time is now configurable via `--watch_sleep_time` and defaults to `100ms` if `--watch_sleep_time` is not provided [ENHANCEMENT]
* Added a `GET` endpoint on Admin portal `localhost:8889/refresh` for refreshing stubbed data [ENHANCEMENT]

#### 2.0.10
* Record & Replay. The HTTP traffic is recorded on the first call to stubbed `uri` and subsequent calls will play back the recorded HTTP response, without actually connecting to the external server [FEATURE]

#### 2.0.9
* Ensuring that Admin portal status page loads fast by not rendering stubbed response content which slows down page load. User can invoke Ajax request to fetch the desired response content as needed [ENHANCEMENT]
* Pre-setting header `x-stubby-resource-id` during YAML parse time, instead of on demand. This way resource IDs are viewable on Admin status page [ENHANCEMENT]
* Making sure that header `x-stubby-resource-id` is recalculated accordingly after stubbed data in memory has changed (due to reset, or deletion etc.) [BUG]
* Added date stamp to live reload success message [COSMETICS]

#### 2.0.8
* Making sure that every stubbed response returned to the client contains its resource ID in the header `x-stubby-resource-id`. The latter is useful if the returned resource needs to be updated at run time by ID via Admin portal [FEATURE]

#### 2.0.7
* Force regex matching only everywhere (url, query, post, headers, etc.) to avoid confusion and unexpected behaviour, with default fallback to simple full-string match (Michael England) [ENHANCEMENT]

#### 2.0.6
* Live YAML scan now also check for modifications to external files referenced from main YAML [ENHANCEMENT]
* YAML parsing logic revisited [COSMETICS]
* Code cleanup [COSMETICS]

#### 2.0.5
* Added ability to specify sequence of responses on the same URI using `file` (Prakash Kandavel) [ENHANCEMENT]
* Minor code clean up [COSMETICS]
* Documentation update [COSMETICS]

#### 2.0.4
* Making sure that operations starting up stubby and managing stubbed data are atomic  [ENHANCEMENT]

#### 2.0.3
* Typo in test was giving wrong indication that when `file` not set, stubbed response fallsback to `body` [BUG]
* Eliminated implicit test to test dependencies in AdminPortalTest that was causing issues when running the tests under JDK 1.7 [BUG]
* Added convenience method in StubbyClient `updateStubbedData` [ENHANCEMENT]

#### 2.0.2
* Stubbed request HTTP header names were not lower-cased at the time of match [BUG]
* Doing GET on Admin portal `/` will display all loaded stub data in a YAML format in the browser [FEATURE]
* Doing GET on Admin portal `/<id>` will display loaded stub data matched by provided index in a YAML format in the browser [FEATURE]
* Doing DELETE on Admin portal `/<id>` will delete stubbed request from the list of loaded stub requests using the index provided [FEATURE]
* Doing PUT on Admin portal `/<id>` will update stubbed request in the list of loaded stub requests using the index provided [FEATURE]
* When YAML is parsed, if `file` could not be loaded, the IOException is not thrown anymore. Instead a warning recorded in the terminal [ENHANCEMENT]
* When could not match submitted HTTP request to stubbed requests, the not found message is much more descriptive [ENHANCEMENT]
* URI for registering new stub data programmatically via POST on Admin portal was changed from `/stubdata/new` to `/` [COSMETICS]
* URI for getting loaded stub data status was changed from `/ping` to `/status` on Admin portal [COSMETICS]
* Updated to SnakeYAML v1.12 [COSMETICS]
* Updated default response message when response content could not be loaded from `file` [ENHANCEMENT]
* Documentation refinement [COSMETICS]

#### 2.0.1
* Every ```url``` is treated as a regular expression now [ENHANCEMENT]
* ANSI logging in the terminal was working only for HTTP requests with status 200 [BUG]
* Documentation refinement [COSMETICS]

#### 2.0.0
* Mainly backend code improvements: A lot of refactoring for better code readability, expanding test coverage [COSMETICS]

#### 1.0.63
* Added ability to specify sequence of stub responses for the same URI, that are sent to the client in the loop [FEATURE]
* Configuration scan was not enabled, even if the ```--watch``` command line argument was passed [BUG]

#### 1.0.62
* Added ability to specify regex in stabbed URL for dynamic matching [FEATURE]
* A lot of minor fixes, refactorings and code cleaned up [COSMETICS]
* Documentation revisited and rewritten into a much clearer format [ENHANCEMENT]

#### 1.0.61
* Just some changes around unit, integration and functional tests. Code cleanup [COSMETICS]

#### 1.0.60
* stubby's admin page was generating broken hyper links if URL had single quotes [BUG]
* stubby is able to match URL when query string param was an array with elements within single quotes, ie: ```attributes=['id','uuid']``` [ENHANCEMENT]

#### 1.0.59
* stubby's admin page was not able to display the contents of stubbed response/request ```body```, ```post``` or ```file``` [BUG]
* stubby was not able to match URL when query string param was an array with quoted elements, ie: ```attributes=["id","uuid","created","lastUpdated","displayName","email"]``` [BUG]

#### 1.0.58
* Making sure that stubby can serve binary files as well as ascii files, when response is loaded using the ```file``` property [ENHANCEMENT]

#### 1.0.57
* Migrated the project from Maven to Gradle (thanks to [Logan McGrath](https://github.com/lmcgrath) for his feedback and assistance). The project has now a multi-module setup [ENHANCEMENT]

#### 1.0.56
* If `request.post` was left out of the configuration, stubby would ONLY match requests without a post body to it [BUG]
* Fixing `See Also` section of readme [COSMETICS]

#### 1.0.55
* Updated YAML example documentation [COSMETICS]
* Bug fix where command line options `mute`, `debug` and `watch` were overlooked [BUG]

#### 1.0.54
* Previous commit (`v1.0.53`) unintentionally broke use of embedded stubby [BUG]
