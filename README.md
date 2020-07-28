# RSSreader 2.0

Pet-project for playing around with state-of-the-art Android technologies. Basically, you can read aggregated news from Onliner.by and Tut.by.

Current tech stack:
* [Kotlin]((https://api.bintray.com/packages/friendoye/maven/recyclerxray/images/download.svg))
* [Jetpack Compose (dev-14)]():
    * [Workflow Kotlin Compose](https://github.com/square/workflow-kotlin-compose)
    * [Compose Router](https://github.com/zsoltk/compose-router)
    * [Compose Backstack](https://github.com/zach-klippenstein/compose-backstack)
    * [Accompanist for Coil](https://github.com/chrisbanes/accompanist/tree/main/coil)
* [Workflow](https://github.com/square/workflow-kotlin)
* [Coil](https://github.com/coil-kt/coil)
* [OrmLite [from 1.0]](https://ormlite.com/sqlite_java_android_orm.shtml)
* [Jsoup [from 1.0]](https://jsoup.org)

## Setup

In order to be able to build and install this app, you should place [Workflow Kotlin Compose](https://github.com/square/workflow-kotlin-compose) near your project and resolve dependencies conflicts. You may copy [this fork](https://github.com/friendoye/workflow-kotlin-compose/tree/nn/rss-reader-setup) to reduce amount of work for setup.

## Is it worth using `Jetpack Compose`?

Definitely no. For now. Nevertheless, I would say, that starting trying out it right now is not a bad idea.

## Is it worth using `Workflow`?

Right now `Workflow` is in alpha stage, but API will probably not be changed really prior to 1.0. I would recommend to try it out, if:
* You building View-based app;
* You have no plans to use it in conjuction with `Compose`.