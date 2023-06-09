

### 格式化定制

```java
public class DemoApp {
    public static void main(String[] args){
        Solon.start(DemoApp.class, args, app->{
            initMvcJsonCustom();
        });
    }

    /**
     * 初始化json定制（需要在插件运行前定制）
     * */
    private static void initMvcJsonCustom(){
        //通过转换器，做简单类型的定制
        FastjsonRenderFactory.global.addConvertor(Date.class,
                (JsonLongConverter<Date>) source -> source.getTime());

        FastjsonRenderFactory.global.addConvertor(LocalDate.class,
                (JsonStringConverter<LocalDate>) source -> source.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        //通过编码器，做复杂类型的原生定制（基于框架原生接口）
        FastjsonRenderFactory.global.addEncoder(Date.class, (ser, obj, o1, type, i) -> {
            SerializeWriter out = ser.getWriter();
            out.writeLong(((Date)obj).getTime());
        });
    }
}
```