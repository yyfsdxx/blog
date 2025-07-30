# *Git*基础
为了更好的使用git代码版本控制，我参考了各位大佬的博客，系统的学习了git的使用与一些基本原理，接下来会用几篇博客，详细的说明如何运用git管理代码以及多人协助开发。
>参考博客[廖雪峰的Git教程](https://liaoxuefeng.com/books/git/create-repo/index.html)
## Git中工作区、暂存区、版本库的理解
在了解这三个概念之前，我们先完成对一个文件夹进行git版本控制的初始工作。例如我要在gittest文件下进行git版本控制（演示系统为windows，其他系统也可类似）。
![1](<resource/屏幕截图 2024-12-29 153836.png>)
通过`git init`工具来在把这个文件夹变成git可以管理的仓库，这样我们的初始工作就做好了。
```shell
$ git init
Reinitialized existing Git repository in D:/浏览器下载/java projiect/springboot_project/blog/.git/
```
此时该文件下会产生一个`.git`文件夹，这个是git工具存储和管理版本库的地方，这个文件夹默认为隐藏文件夹，如果你的目录下没有看到，把隐藏文件显示即可。
![2](<resource/屏幕截图 2024-12-29 154911.png>)
现在为止初始工作做好了，现在我们可以开始尝试实现下面这一个需求
>创建一个文件readme.txt,并提交给git版本库管理

我们在目录下手动创建一个文本文件`readme.txt`，并且在git命令台运行以下命令将该文件交给git管理。
```shell
$ git add readme.txt
$ git commit -m "add reademe.txt"
[master (root-commit) 5166af4] add reademe.txt
 1 file changed, 0 insertions(+), 0 deletions(-)
 create mode 100644 readme.txt
```
这样readme.txt就被我们成功添加进git版本库管理了，接下来通过这个过程介绍工作区、暂存区、版本库概念理解。在此之前，我们需记住以下这句话。
>**Git管理的是修改而不是文件！**

### 1. 工作区
工作区类似一个存储空间，工作区记录你刚开始在自己在文件上的修改，在上面的操作中，则是你自己创建一个readme.txt这样一个修改，被记录在工作区中。记住此时“**创建readme.txt**”这个修改此时是在工作区的。
### 2. 暂存区
暂存区同样也是一个存储空间，上面记录了你对文件的修改，在你运行`git add readme.txt`时候，“**创建readme.txt**”这个修改就从工作区转到了暂存区，工作区此时则会是干净的。
### 3. 版本库
版本库同样会存储修改信息，但是不止存储一个修改，而是所有所有分支的所有修改，方便回滚，分支概念会在后面博客提到。到运行`git commit -m "add reademe.txt"`命令时，则会把暂存区的修改转到版本库，同样暂存区此时也会是干净的。我们可以通过`git status`命令查看母亲工作区和暂存区状态
```shell
$ git status
On branch master
nothing to commit, working tree clean
```
可以看到暂存区和工作区此时都是干净的，`git commit`后面引号内容是这次提交的说明，方便你以后知道这次提交是为了干嘛。这三者的关系可以下面这张图表示。
![3](<resource/屏幕截图 2024-12-29 162553.png>)。

### 4. 场景应用
>接下来需要你修改readme.txt的内容为：apple，然后执行**git add**命令，接着修改readme.txt为：banana，再执行**git commit**命令。现在请问版本库里记录的是哪次的修改？工作区和暂存区有没有记录修改，有的话又是哪一次修改。

很显然，如果搞懂了工作区、暂存区、版本库的概念，则版本库里面的修改为<strong>“修改readme.txt 的内容为：apple”</strong>,而工作区为<strong>“修改readme.txt为：banana”</strong>，我们可以通过`git diff`命令来比较当前工作区与版本库的区别
```shell
$ git diff HEAD -- readme.txt
diff --git a/readme.txt b/readme.txt
index 0e37c88..570a66f 100644
--- a/readme.txt
+++ b/readme.txt
@@ -1 +1 @@
-apple
\ No newline at end of file
+banana
\ No newline at end of file
```
了解这些概念将会在版本回退很有用处。
