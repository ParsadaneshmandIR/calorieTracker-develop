# پیگیر کالری (Persian Calorie Tracker)

برنامهٔ وب **Java 21 / Spring Boot 3.3** برای تخمین کالری مواد غذایی و وعده‌ها با رابط فارسی RTL.

## اجرا (محلی)

پیش‌نیاز: JDK 21 و Maven Wrapper همراه پروژه.

```bash
./mvnw spring-boot:run
```

سپس باز کنید: [http://localhost:8080](http://localhost:8080)

دیتابیس پیش‌فرض: H2 فایل‌محور در `./data/calorie-tracker`.

تست‌ها:

```bash
./mvnw verify
```

## کلید API وزارت کشاورزی آمریکا (USDA)

1. در [FoodData Central API](https://fdc.nal.usda.gov/api-key-signup.html) ثبت‌نام کنید.
2. کلید را فقط از طریق متغیر محیطی بدهید — **هرگز در کد commit نکنید**:

```bash
export USDA_API_KEY=your-key-here
./mvnw spring-boot:run
```

ورود دادهٔ USDA فقط از مسیر ادمین یا جاب آفلاین:

```bash
# جاب خط فرمان
./mvnw spring-boot:run -Dspring-boot.run.arguments="--import.usda=true --import.usda.query=apple"

# یا
curl -u admin:admin -X POST "http://localhost:8080/admin/import/usda?query=chicken"
```

درخواست‌های معمولی کاربر **هرگز** به API خارجی وابسته نیستند؛ فقط کش محلی خوانده می‌شود.

## Open Food Facts

برای ایمپورت بارکد (ادمین):

```bash
export OFF_USER_AGENT_EMAIL=you@example.com
curl -u admin:admin -X POST "http://localhost:8080/admin/import/off?barcode=3017620422003"
```

خواندن برای کاربر: `GET /api/foods/barcode/{barcode}` فقط از دیتابیس محلی.

## افزودن غذای دستی

- از API: `POST /api/foods` با JSON معتبر (kcal/پروتئین/کربوهیدرات/چربی **در ۱۰۰ گرم**).
- یا ردیف جدید در `src/main/resources/data/iranian-foods.csv` و راه‌اندازی مجدد (upsert روی `source + sourceId`).

واحد پایه همیشه **کیلوکالری در ۱۰۰ گرم بخش خوراکی** است. برنج خام و برنج پخته دو ردیف جدا هستند.

## Docker

```bash
export DB_PASSWORD=calorie
export USDA_API_KEY=...
docker compose up --build
```

پروفایل `prod` از PostgreSQL استفاده می‌کند (`application-prod.yml`).

## لایسنس داده‌ها

- USDA FoodData Central: دامنهٔ عمومی (public domain)
- Open Food Facts: ODbL 1.0
- غذاهای ایرانی CSV: برآورد دستی مستندشده در ستون `notes`

جزئیات: [`docs/data-sources.md`](docs/data-sources.md)

## سلب مسئولیت پزشکی

این ابزار تخمین تقریبی کالری است و جایگزین توصیهٔ متخصص تغذیه نیست. مقادیر بر اساس میانگین دستور پخت است و روغن، شکر و اندازهٔ پرس می‌تواند نتیجه را تغییر دهد.

## ادمین

- نام کاربری/رمز پیش‌فرض: `admin` / `admin` (با `ADMIN_USERNAME` و `ADMIN_PASSWORD` عوض کنید)
- Actuator فقط `health` و `info`
