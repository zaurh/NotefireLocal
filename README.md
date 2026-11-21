
Notefire Local is a note-taking app where you can take notes offline. You don't need an internet connection. I've used Room Library to Insert and Retrieve them locally. Once you create a new note it will stay forever in your application database until you delete your note or application data.

[<img src="https://i.hizliresim.com/rbn6uq0.png" height="100">](https://play.google.com/store/apps/details?id=com.zaurh.notefirelocal)


# Preview

![App Screenshot](https://i.hizliresim.com/dfo0nnr.png)


# Features

- Write notes that save themselves as you type, fully offline
- Style the title and the text separately: color, font size, bold, italic, underline, strikethrough
- Any background color: quick picks or a full color wheel
- Pin important notes and share them to other apps
- Search, grid or list view, undo after delete
- Light, dark or system theme
- The note card grows into the editor (shared element transition)

# Tech

- Kotlin, Jetpack Compose, Material 3
- **MVI**: each screen has a `Contract` (immutable `State`, `Intent`s, one-off `Effect`s), a `ViewModel` built on `MviViewModel`, and a stateless screen
- Clean Architecture layers: `domain` (models, repository interfaces), `data` (Room, DataStore), `presentation`
- Room with schema export and tested migrations (notes from 1.x are kept on update)
- DataStore Preferences, Hilt, type-safe Navigation Compose, Coroutines/Flow
- Unit tests for ViewModels and an instrumented migration test

## License
```
MIT License

Copyright (c) 2024 Zaur Hasanov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
