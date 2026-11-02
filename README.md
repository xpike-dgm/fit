# Fit

Kişisel kilo ve beslenme takibi için Android uygulaması. Proje Jetpack Compose ile hazırlanmıştır ve kayıtları cihazda tutar.

## İlk arayüz sürümü

- Açık ve koyu tema
- 61 kg başlangıç ve 73,0 kg hedef kartı
- Öğün bazında manuel kalori, protein, karbonhidrat ve yağ kaydı
- Geçmiş plan günlerini takvimden açma, öğün kayıtlarını düzenleme veya silme
- Profil ekranından plan başlangıç tarihini değiştirme
- Günlük hedefler: başlangıçta 3.000 kcal, 120 g protein, 427,5 g karbonhidrat, 90 g yağ
- Günlük toplamlar, hedefe kalan miktar ve öğün tamamlandı işareti
- Su ve haftalık antrenman günü işaretleme
- Tartı geçmişi ve kilo grafiği
- Aynı haftadaki tamamlanmış günlerin kalori fazlasını kalan günlere en fazla 100 kcal/gün azaltma ile yansıtma; açık kalırsa haftaya taşımama
- Eksik tüketim kaydını sıfır saymama ve kalori hedefi değerlendirmesini kayıt yeterliliğine bağlama
- 14 günlük eğilim, haftalık tartı ortalaması ve koşullu hedef güncelleme akışı
- İlerleme fotoğraflarını cihazın uygulama alanında saklama

73,0 kg hedefi ile 90 günlük öncelikli dönem uygulamada korunur. Süre, hedefe ulaşıldığı garantisi değildir; 90. günde hedef ortalaması görülmezse takip devam eder.

V2 önerileri, V3 ürün kapsamı ve aşamalı yol haritası için [`ROADMAP.md`](ROADMAP.md) dosyasına bak.

## Derleme

Android Studio ile klasörü açabilir veya Windows'ta şu komutu çalıştırabilirsin:

```powershell
.\gradlew.bat assembleDebug
```

Debug APK: `app\build\outputs\apk\debug\app-debug.apk`

Emülatöre veya Android cihaza yüklemek için:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```
