# PopuMovies
Android developer nanodegree, Projects 1 &amp; 2: Popular Movies app assignment

## Assignment guidelines
https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true

## Main Description and features
The goal of this app is to show highest rated movies ever and most popular movies. It feeds from [The Movie DB API](https://developers.themoviedb.org/3)

### Features
- List movies as a grid
- Allows to mark movies as favorite
- Filter options for most popular, highest rated and favorites
- Movie details includes trailers and reviews

### (What I think are) Special Features
- Works 100% offline by SQLite3 persistence
- Images also show when offline by using picasso lib with a custom okHTTP3 disk cache
- Movie navigation with swipe gesture, applying nested fragments and view pager
- Compatibility down to API 10 as a result of extensive use of support library
- RecyclerView custom GridLayouts  view, with use of Cursor Adapters and Loaders
- Content Providers technique applied
- Adaptive for tablets (two pane view)
- Keeps list position and current selected movie when rotating
- Swipe to refresh
- Tutorial view when first time
- It keeps HTTP calls to minimum by just no getting the same information twice
- JSON calls are modeled with Retrofit2
- Horizontal scroll for trailers
- Expandable Textview for reviews
- It is done with love =)

## Screencast of Portrait mode in 5,5 inch screen
It shows Swipe between movies, trailers horizontal scroll, expandable textview and favorites features
![Screencast portrait](/screencast_portrait.gif)

## Screenshot of Portrait detail for 3.5 inch screen

## Screenshot of Landscape two pane for 7 inch screen
