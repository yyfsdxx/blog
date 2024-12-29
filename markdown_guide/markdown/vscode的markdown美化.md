# *vscode Markdown* 预览样式美化
最近想用vscode来写markdown，但是发现自带样式实在是比较丑，因此在网上寻找方法进行样式的美化，发现了两种方法。

- **直接修改vscode内置markdown样式** 
这种方法的操作可以看[这篇博客](https://blog.csdn.net/csdnear/article/details/78229021),但是也有明显的局限性，即无法导出成pdf，因此不推荐使用。

- **采用插件Markdown Preview Enhan**  
当然，直接在vscode上面安装Markdown Preview Enhan即可以使用，这款插件非常好用，可以导出pdf、html。虽然它提供了很多种css样式，但是还是觉得不是很好看，因此接下来介绍如何使用自己自定义的样式，实现好看的markdown预览并且可以导出为pdf。

### 插件Markdown Preview Enhan自定义样式操作
1. **找到插件的主题样式的文件设置**
   文件大致位置在
   ```   
   C:\Users\你的用户名\.vscode\extensions\shd101wyy. 
   markdown-preview-enhanced-0.8.15\crossnote\styles
   ```
   然后在此文件夹下会有两个要用到的文件夹
   ![alt text](<resource/屏幕截图 2024-12-28 222009.png>)
   文件夹**preview_theme**是用来做markdown基本主题样式，文件夹**prism_theme**是来做代码块样式的，然后把**mystyle.css**和**codeblock.css**分别放入其中（两个样式在博客末可以获取）
2. **vscode应用样式文件**
   接下来打开vscode，然后按快捷键`Ctrl-shift-p`,然后点击`Preferences:Open User Settings(JSON)`打开settings.json
   ![111](<resource/image.png>)</br>
   修改如下两项的值即可
   ![111](<resource/屏幕截图 2024-12-28 224107.png>)
3. 使用效果如下
   ![111](<resource/屏幕截图 2024-12-29 111433.png>)

### 文章样式下载
[mystyle1.css]()