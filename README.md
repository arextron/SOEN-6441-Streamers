# TubeLytics v2 ( SOEN-6441 “Streamers” )

A real-time, actor-driven YouTube analytics dashboard built with the **Play 2.8** framework, **Akka Actors/Streams**, and the **YouTube Data API v3**.  
Originally created for Concordia University’s *SOEN-6441 – Software Engineering* course, the project showcases modern reactive‑web techniques (WebSockets, non-blocking I/O, caching) while providing an intuitive UI for exploring channels, videos, tags, and word-level statistics.

---

## ✨ Key Features

| Category | Highlights |
|----------|------------|
| **Channel & Video Search** | Instant search with autocomplete and quota‑friendly caching (EhCache). |
| **Channel Profile** | Subscribers, total views, description, and top videos at a glance. |
| **Tag Explorer** | Discover and drill into the most frequent tags across any search result or channel. |
| **Word‑Stats Dashboard** | Actor‑powered frequency analysis of words in video descriptions (stop‑word filtered). |
| **Live Updates** | WebSockets keep the UI reactive without page refreshes. |
| **Scalable Concurrency** | Akka actors (`UserActor`, `TagsActor`, `WordStatsActor`, `SupervisorActor`) isolate work, enable supervision, and prevent blocking. |
| **Clean MVC** | Separation of concerns via Play Controllers, Services, and Scala.html templates. |
| **Test Suite** | JUnit 5 tests illustrate controller logic and service behaviour. |

---

## 🛠 Tech Stack

* **Java 11** & **Scala 2.13**
* **Play Framework 2.8** (Java flavour)
* **Akka Actors & Streams**
* **YouTube Data API v3**
* **EhCache** (Play cache module)
* **Bootstrap 5** / **vanilla JS** (server‑side rendered templates)
* **sbt** build tool

---

## 🗂 Project Layout

```
app/                 # MVC, services & actors
 ├─ actors/          # Akka actor classes
 ├─ controllers/     # Play controllers & WebSocket endpoints
 ├─ models/          # Plain‑old Java objects (POJOs)
 └─ views/           # Scala.html templates

conf/
 ├─ application.conf # App settings (YouTube key, cache, ports)
 └─ routes           # HTTP + WebSocket routing table

public/              # Static assets (CSS, JS, images)
test/                # Unit & integration tests
build.sbt            # sbt build definition
```

---

## 🚀 Quick Start

### 1 – Prerequisites

| Tool | Minimum version |
|------|-----------------|
| **JDK** | 11 |
| **sbt** | 1.9+ |
| **YouTube API Key** | Data API v3 enabled |

### 2 – Clone & Configure

```bash
git clone https://github.com/<your‑handle>/SOEN‑6441‑Streamers.git
cd SOEN‑6441‑Streamers

# safest: export the key as an env‑var
export YOUTUBE_API_KEY="YOUR‑API‑KEY"
```

> **Heads‑up 🔑**  
> An example key is hard‑coded in `conf/application.conf` and `YouTubeService.java`.  
> **Replace it with your own** or, preferably, pass it at runtime:
>
> ```bash
> sbt -Dyoutube.api.key=$YOUTUBE_API_KEY run
> ```

### 3 – Run Locally

```bash
sbt run
```

Open **http://localhost:9000** and start exploring.

### 4 – Run Tests

```bash
sbt test
```

---

## 🌐 REST & WebSocket Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `GET /` | Home page & search bar |
| `GET /search?query=q` | Channel & video search (HTTP) |
| `GET /channel/:channelId` | Channel profile |
| `GET /video/tags/:videoId` | Video details & tag list |
| `GET /search/tag/:tag` | Videos for a specific tag |
| `GET /wordStats?query=q` | Word‑frequency analysis |
| `GET /ws/search` | **WebSocket** stream for live search results |

---

## 📦 Packaging & Deployment

1. **Self‑contained ZIP/JAR**

   ```bash
   sbt dist
   unzip target/universal/TubeLytics-*.zip
   ./TubeLytics-*/bin/tubelytics \
     -Dyoutube.api.key=$YOUTUBE_API_KEY \
     -Dhttp.port=9000
   ```

2. **Docker (optional)** – create a simple `Dockerfile`:

   ```dockerfile
   FROM eclipse-temurin:11-jre
   WORKDIR /opt/app
   COPY target/universal/TubeLytics-*.zip app.zip
   RUN apt-get update && apt-get install -y unzip && \
       unzip app.zip && rm app.zip
   ENV YOUTUBE_API_KEY=${YOUTUBE_API_KEY}
   CMD ["./TubeLytics-*/bin/tubelytics", "-Dhttp.port=9000"]
   ```

3. **Cloud** – Play apps run nicely on Heroku, AWS Elastic Beanstalk, GCP App Engine (Flex), or any JVM‑friendly host.

---

## 🤝 Contributing

1. Fork ➜ Create feature branch ➜ Commit ➜ Pull Request.
2. Follow the existing code style (Google Java Style) and write tests for new features.
3. Make sure your PR passes `sbt test` and `sbt compile` before submission.

---

## 📜 License

This project is released under the **MIT License**.  
See `LICENSE` for details (or add one if missing).

---

## 👥 Authors

| Name | Role |
|------|------|
| **Aryan Awasthi** | Lead developer, architecture |
| **Harsukhvir Singh Grewal** | Backend & caching |
| **Sharun Basnet** | Frontend templates & tag explorer |

> Developed for Concordia University SOEN‑6441 (Winter 2025).

---

## 🙏 Acknowledgements

* Google — YouTube Data API v3  
* Lightbend — Play Framework & Akka  
* Bootstrap, FontAwesome, and the open‑source community
