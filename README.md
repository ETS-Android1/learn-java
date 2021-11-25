See below for english.

# Learn Java android alkalmazás

Ez az alkalmazás Java programozás tanulására használható.

Megtalálható a Play Store-ban, [itt](https://play.google.com/store/apps/details?id=com.gaspar.learnjava) 
Az áruházi adatlapon láthatóak képernyőképek.

## Build folyamat

Ez a projekt inportálható Android Studio-ba. Három build variáns van, ezek a **release**, **debug** és **noads**. 
Tulajdonságaik:

	- Release: Ez az éles változat, ami a Play Store-ban is metalálható. Valódi reklámok és 
	API hívások vannak benne. Mivel érzékeny információ kell hozzá ami nincsen GitHub-ra feltöltve, 
	ezért ezt **csak én** tudom buildelni.
	- Debug: A fejlesztői változat, amiben teszt hirdetések és API hívások vannak. Mivel érzékeny információ kell hozzá ami nincsen GitHub-ra feltöltve, 
	ezért ezt **csak én** tudom buildelni.
	- Noads: Ez egy olyan változat, amiben szándékosan semmilyen hirdetés vagy API hívás sincs, ezért 
	bárki tudja buildelni. Egyes funkciók, például a hirdetések és a Java futtató nem használhatóak benne.

A megfelelő build variáns az Android Studio felületén keresztül lehet kiválasztani.

## Java oktató anyagok

A tananyagról:
- Kurzusokból áll, amiket fejezetekre bontottam tovább.
- Vizsgák minden kurzushoz, ahová egy kérdéshalmazból kerülnek be a kérdések, véletlenszerűen.
- Feladatok, amik lényegében kis önálló projektek, referencia megoldásokkal.

## Alkalmazás funkciók

- Több alkalmazás téma, köztük sötét téma.
- Másolható, megosztható és formázott kódminták a tananyagban.
- Interaktív kódminták, amik egyfajta mini feladatokként működnek.
- ClipSync, azaz kódminták tartalmának megosztása a számítógéppel (lásd lent).
- Játszótér, ahol Java kódot lehet írni és futtatni ([Glot.io](https://glot.io/) API segítségével).

## ClipSync

Ezzel a funkcióval megoszthatók a kódminták az okos eszköz és a számítógép közt. Szükséges 
hozzá a ClipSync szerver programom, ami [innen](https://gtomika.github.io/learn-java-clipsync/) 
szerezhető be.

## Kódminták formázása

A kódmintákat egy saját segédprogrammal formáztam meg, így adva nekik egységes színezést és 
tabulálást az alkalmazáson belül. A formázó program megtekinthető 
[itt](https://github.com/Gtomika/learn-java-code-formatter).

# Learn Java android application

An android application that teaches Java programming.

It is already published in the playstore, see it [here](https://play.google.com/store/apps/details?id=com.gaspar.learnjava) 
for screenshots and download.

## Build process

You can import this project to Android Studio, but note that you'll only be able to build the **noads** 
build variant. The other two variants (**release**, **debug**) require files/sensitive information that 
are only present on my computer.

## Java teaching materials

About the curriculum:
- Courses, separated into chapters.
- An exam for each course (random questions from a question pool). Unlocks the next course.
- Tasks: example projects that you can solve (with solutions).

## Application features

Features:
- Multiple application themes (dark and orange).
- Formatted code samples that are copiable and zoomable.
- Interactive code samples with missing parts (mini-tasks).
- Clip sync with your computer (bluetooth or local network).
- Notifications: get alerted when you can take a failed exam again.
- Playground: edit and run simple Java programs (powered by [Glot.io](https://glot.io/).

## Built in clipsync

Clip syncing uses my small app that can be downloaded from [here](https://gtomika.github.io/learn-java-clipsync/).
Launch it on your computer and select a syncing mode!

## Code samples formatter

Code samples in the application have been formatted programmtically to display the way they do 
in the app. This is done by my [formatter tool](https://github.com/Gtomika/learn-java-code-formatter).