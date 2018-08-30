### java annotation基础
java注解分为标准注解和元注解。

标准注解是java为我们提供的预定义的注解，@override,@deprecated,@suppresswarnnings，@safevarargs

元注解是给我们自定义注解用的共有5种,@target,@retention,@documented,@inherited,@repeatable

@Target：用来修饰注解能够修饰的对象的类型，接收一个elementtype类型的数组。

@Remention：用来指定注解的保留策略。可以指定如下值
		- SOURCE 注解只保留在源码层面，编译时即被丢弃
		- CLASS 注解可以保留在class文件中，但会被jvm丢弃
		- RUNTIME 注解也会在jvm运行事件保留，可以通过反射读取注解信息

@Documented：此注解将被包含在javadoc中

@Inherited：此注解可以被继承

@Repeatable：指定的注解可以重复应用到指定的对象上边


标准注解也是用元注解创建的

	@Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Override{}
    
如上所示：

@Target(ElementType.METHOD)表示这个注解能够修饰方法

@Retention(RetentionPolicy.SOURCE)表示这个注解只存在于源码
### 解析注解的两种方式
#### 1. 运行时注解可以使用反射解析
下边的使用反射解析注解的实例：创建一个类，然后在类中对字段做注解，最后通过反射的方法解析注解的内容

首先需要创建注解类

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UseCase {
        String name();
        int id() default -1;
    }
    
然后定义一个类，使用上边的注解

    public class Persion {
        @UseCase(id = 0, name = "nancy")
        int number;

        @UseCase(id = 2, name = "lucky")
        String name;
    }
    
最后通过反射解析注解

    try {
                Class clz =Class.forName("com.dou.demo.knife_annotation.Persion");
                Field[] fields = clz.getDeclaredFields();

                for (Field field : fields) {
                    UseCase annotation = field.getAnnotation(UseCase.class);

                    if (annotation != null) {
                        System.out.println(annotation.name() + annotation.id());
                    }
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

#### 2. 编译时注解可以使用annotationprocessor解析
编译时注解的使用场景很多，包括很多的第三方库例如Butterknife, Retrofit, GreenDao等，下边我们就做一个简单的demo，类似于butterknife的例子来讲解如何使用annotationprocessor。

首先来讲一下butterknife的大致的注入流程

首先我们需要在activity中使用@BindView(R.id.xx)注解对应的控件，然后使用Butterknife.bind(this)方法完成对控件的绑定，实际上底层还是调用findviewbyid方法。大概的思路就是在编译期生成一个类似MainActivity_ViewBinding.java文件，在这个类的构造方法中最终会调用findViewById()方法。而调用Butterknife.bind(this)方法，首先会找到MainActivity_ViewBinding这个类的构造方法对象，然后实例化该构造方法(执行类的构造方法)，也就是执行findviewbyid动作

### 基于annotationprocessor使用注解的步骤
原理为：先在activity中使用注解@BindView或者@OnClick标注控件对象，然后调用knife.bind(this)方法进行绑定，编译注解时生成一个Activity$$Injector类，bind方法主要就是调用这个类的inject方法，这个方法底层调用findviewbyid

因为自己学习时已经定义了@BindView,下边主要定义另外一个注解@OnClick

#### 定义注解类
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface OnClick {
        int[] id() default -1;
    }
#### 封装绑定接口
在activity中使用了注解之后，在编译的时候自动生成对应的XXActivity$$Inject类，这个类中有一个inject方法，这个方法提供findviewbyid和setonclicklistener方法。这里的我们首先定义一个Injector接口,在接口中定义一个inject方法，自动生成的xxactivity$$injector类实现了这个方法。这个接口封装绑定接口指的就是Knife.bind(this)的这个过程，我们使用多态的特性调用Injector.inject()方法完成绑定。具体代码如下：

Knife类：

    public class Knife {

        static Finder ACTIVITY_FINDER = new ActivityFinder();
        static Finder VIEW_FINDER = new ViewFinder();

        public Knife() {
            throw new AssertionError("not available for instance.");
        }

        public static void bind(Context context){
            bind(context, ACTIVITY_FINDER);
        }

        public static void bind(View view){
            bind(view, VIEW_FINDER);
        }

        public static void bind(Object host, Finder finder){
            bind(host, host, finder);
        }

        public static void bind(Object host, Object source, Finder finder){
            String className = host.getClass().getName();

            try {
                Class findClass = Class.forName(className + "$$Injector");
                Injector injector = null;
                try {
                    injector = (Injector) findClass.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (host == null) {
                    Log.d("xxx", "bind: host");
                    return;
                } else if (finder == null) {
                    Log.d("xxx", "bind: finder");
                    return;
                }

                injector.inject(host, source, finder);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

Injector接口:

    public interface Injector<T> {
        void inject(T host, Object source, Finder finder);
    }
    
其他的类：


    public interface Finder {
        Context getContext();
        View findView(Object source, int resId);
	}
    
    public class ActivityFinder implements Finder {

        Context context;

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public View findView(Object activity, int resId) {
            context = ((Activity) activity);
            return ((Activity) activity).findViewById(resId);
        }
    }
    
    public class ViewFinder implements Finder {

        Context context;

        @Override
        public Context getContext() {
            return context;
        }

        @Override
        public View findView(Object view, int resId) {
            context = ((View) view).getContext();
            return ((View) view).findViewById(resId);
        }
    }

#### 定义annotationprocessor
这里是对编译时注解进行解析并生成对应类的地方，我们在这里使用annotationprocessor和javapoet。annotationprocessor会在注解编译时期提供回调，我们的主要工作都是在它的process()中进行的。javapoet是square开源的生成java类的开源库。

首先我们需要定义一个类集成AbstractProcessor,他是annotationprocessor的核心类，我们需要实现它的4个方法：
- init() 初始化代码，一般会去获取Elements,messager,filer
- process() 处理方法
- getSupportedAnnotationTypes() 用来指定该处理器适用的注解
- getSupportedSourceVersion() 用来指定你的编译器的java版本

之后还需要对处理器进行注册，第一种方法是在java同级目录下创建一个resources/META-INF/service文件夹，然后在文件夹中创建名为javax.annotation.processing.Processor的文件，文件内容为 我们的处理器的目录。
另一种方法是使用谷歌的@AutoService注解，你需要添加依赖
		
        compile 'com.google.auto.service:auto-service:1.0-rc2'
然后在自己的处理器上面加上@AutoService(Processor.class)注解即可。

###### 代码的生成过程

首先做一些初始化操作，指定要注解类型，java编译器版本

  	@Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        System.out.println("processor: init");
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("processor: getSupportedAnnotationTypes");
        Set<String> types = new LinkedHashSet<>();
        types.add(BindView.class.getCanonicalName());
        return types;
    }
        
    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("processor: getSupportedSourceVersion");
        return SourceVersion.latestSupported();
    }

在process中进行代码生成的工作，从参数roundEnvironment中，我们可以获得注解对应的Element信息。

    @Override
        public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
            System.out.println("processor: process");
            processBindview(roundEnvironment);
            processOnClick(roundEnvironment);

            System.out.println("processor: annotationCount = " + annotationClassMap.size());

            for (AnnotationClass annotation : annotationClassMap.values()) {
                try {
                    annotation.generateInjector().writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

    private void processOnClick(RoundEnvironment roundEnvironment) {
            System.out.println("processor: processOnClick");

            for (Element element : roundEnvironment.getElementsAnnotatedWith(OnClick.class)){

                AnnotationClass annotationClass = getAnnotationClass(element);
                OnClickMethod field = new OnClickMethod(element);
                annotationClass.addField(field);
            }
        }

        private void processBindview(RoundEnvironment roundEnvironment) {

            System.out.println("processor: processBindview");

            for (Element element : roundEnvironment.getElementsAnnotatedWith(BindView.class)){
                AnnotationClass annotationClass = getAnnotationClass(element);
                BindViewField bindViewField = new BindViewField(element);
                annotationClass.addField(bindViewField);
            }
        }

        private AnnotationClass getAnnotationClass(Element element){
            String className = ((TypeElement) element.getEnclosingElement()).getQualifiedName().toString();
            AnnotationClass annotationClass = annotationClassMap.get(className);
            if (annotationClass == null) {
                annotationClass = new AnnotationClass((TypeElement) element.getEnclosingElement(), elements);
                annotationClassMap.put(className, annotationClass);
            }
            return annotationClass;
        }

上边的逻辑主要是根据roundEnvironment分别处理@BindView和@OnClick注解，这些注解分别被保存到AnnotaionClass对象的List<BindViewField>和List<OnClickMethod>集合中，最后调用annotationClass.generateInjector()来生成java类。

    public JavaFile generateInjector(){
            System.out.println("processor: generateInjector");
            // to create method declear
            // @Override
            // public void inject(MainActivity host, Object source, Finder finder) {
            // }
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(TypeName.get(typeElement.asType()), "host", Modifier.FINAL)
                    .addParameter(TypeName.OBJECT, "source")
                    .addParameter(ClassName.get("com.dou.demo.knife_api.finder", "Finder"), "finder");

            // to create method body
            // host.targetview = (TextView) finder.findView)(source, R.id.xx)
            for (BindViewField field : bindViewFields) {
                methodBuilder.addStatement("host.$N=($T)finder.findView(source,$L)",
                        field.getFieldName(),
                        ClassName.get(field.getFieldType()),
                        field.getViewId());
            }

            // to create variable declear
            // OnClickListener listener;
            if (onClickMethods.size() > 0) {
                methodBuilder.addStatement("$T listener",
                        ClassName.get("android.view", "View", "OnClickListener"));
            }

            // to create varible define
            // listener = new OnClickListener(){
            //      @Override
            //      public void onClick(View v ){
            //          host.onClick();
            //      }
            // }
            for (OnClickMethod onClickMethod : onClickMethods) {
                TypeSpec listener = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(ClassName.get("android.view", "View", "OnClickListener"))
                        .addMethod(MethodSpec.methodBuilder("onClick")
                                .addAnnotation(Override.class)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.VOID)
                                .addParameter(ClassName.get("android.view", "View"), "view")
                                .addStatement("host.$N()", onClickMethod.methodName)
                                .build())
                        .build();

                methodBuilder.addStatement("listener=$L", listener);

                for (int id : onClickMethod.viewIds) {
                    methodBuilder.addStatement("finder.findView(source, $L).setOnClickListener(listener)", id);
                }
            }

            String packagename = getPackageName(typeElement);
            String classname = getBinderClassName(packagename, typeElement);

            ClassName binderClassname = ClassName.get(packagename, classname);
            TypeSpec injectorClass = TypeSpec.classBuilder(binderClassname.simpleName() + "$$Injector")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(ClassName.get("com.dou.demo.knife_api", "Injector"), TypeName.get(typeElement.asType())))
                    .addMethod(methodBuilder.build())
                    .build();

            return JavaFile.builder(packagename, injectorClass).build();
        }

### 使用方式

    implementation project(":knife-annotation")
        implementation project(":knife-api")
        annotationProcessor project(":knife-compiler")


    public class MainActivity extends AppCompatActivity {

        @BindView(id = R.id.tv_content)
        TextView tv_content;

        @OnClick(id = R.id.tv_content)
        public void onclick(){
            Toast.makeText(this, "ggggggg", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Knife.bind(this);
            tv_content.setText("hello knife");
        }
    }
### 总结
上边可能有一些需要注意的地方：

1. 定义processor时最好创建java library类型，不然会提示AbstractProcessor类找不到
2. 可能会出现注解编译的时候没有生成对应的注解的情况，需要配合gradle console的log查看哪里出错了

源码:

[douyn/annotation-demo
](https://github.com/douyn/annotation-demo.git)

参考:

[Java 注解及其在 Android 中的应用](https://juejin.im/post/5b824b8751882542f105447d?utm_source=gold_browser_extension)

[Shouheng88/Android-references
](https://github.com/Shouheng88/Android-references)