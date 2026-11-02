# Fit — Uygulama içi güncelleme

Fit, Google Play kullanmadan herkese açık GitHub Releases üzerinden güncelleme denetler. Profil ekranındaki **Uygulama güncellemesi** kartı en son sürümü gösterir; uygulama açılışında en fazla 12 saatte bir sessiz kontrol yapar. Kullanıcı isterse karttan elle kontrol edebilir.

## Güncelleme akışı

1. Yayınlanan GitHub Release `vMAJOR.MINOR.PATCH` biçiminde etiketlenir.
2. Release, adı tam olarak `fit-release.apk` olan imzalı APK varlığını içerir.
3. Fit GitHub Releases API'sinden en son kararlı sürümü ve varlığın SHA-256 özetini alır.
4. Daha yüksek sürüm varsa APK'yı HTTPS üzerinden indirir ve GitHub API'sindeki SHA-256 değeriyle karşılaştırır.
5. Android kurulum ekranını açar. Kullanıcı onayından sonra Android mevcut uygulamayı günceller.

APK'nın paket kimliği aynı kalmalı, sürüm kodu yükselmeli ve her sürüm aynı özel imzalama anahtarını kullanmalıdır. Android bu koşullardan biri sağlanmazsa uygulama üzerine yüklemeyi reddeder. Android 8 ve sonrasında kullanıcı Fit'e dış kaynaktan uygulama yükleme izni vermelidir; kurulum yine sistem onay ekranından geçer.

## Sürüm oluşturma

`app/build.gradle.kts` içindeki `appVersionName` değerini artır. Sürüm kodu şu formülle üretilir: `major * 1.000.000 + minor * 1.000 + patch`. Örneğin `0.2.0` sürüm kodu `2000`, `0.2.1` sürüm kodu `2001` olur. Minor ve patch değerleri 1000'den küçük kalmalıdır.

Kaynak kodunu `main` dalına gönderip çalışma ağacını temizledikten sonra PowerShell'de:

```powershell
.\scripts\publish-release.ps1 -Notes "Sürüm notlarını buraya yaz"
```

Komut imzalı release APK'sını üretir ve GitHub Release olarak yayımlar. `gh` komut satırı aracının yüklü ve GitHub hesabında yetkilendirilmiş olması gerekir.

## İmzalama anahtarının korunması

- Özel anahtar `.secrets/fit-release.p12`, parola ise `fit-signing.properties` içindedir. İki dosya `.gitignore` ile repodan hariç tutulmuştur; GitHub'a yüklenmemelidir.
- Mevcut release sertifikasının SHA-256 parmak izi: `873a45539f8d59239c370124fb59882590e61aa80afc98c32c51127eab6f79c0`.
- Her ikisinin yedek kopyasını güvenli, ayrı bir yerde sakla. Anahtar veya parola kaybolursa mevcut kurulumların üzerine güncelleme imzalanamaz; kullanıcıların uygulamayı kaldırıp yeniden kurması gerekir.
- Aynı anahtarı gelecekteki her sürümde kullan. Yeni bir anahtara geçişi sıradan sürüm güncellemesi gibi yapma.

## İlk geçiş ve sınırlar

V1 APK'sı debug anahtarıyla imzalanmıştı. İmzalı release APK'sına geçerken Android imza uyuşmazlığından dolayı debug uygulamanın üstüne kurulum yapılamayabilir; ilk kez release sürümünü elle kurmak gerekebilir. Debug sürümde girilmiş yerel kayıtlar varsa kaldırma işleminden önce dışa aktarma yolu olmadan uygulamayı kaldırma. Geçişten önce kayıtları taşıma ihtiyacı varsa ayrıca aktarım akışı hazırlanmalıdır.

Güncelleme APK'sı ve sürüm notları herkese açık GitHub deposundan sunulur; kullanıcı beslenme ve ilerleme kayıtları APK'ya eklenmez, cihazdaki uygulama verisi olarak kalır. Bu yöntem normal kişisel Android kurulumunda sessiz/gizli kurulum sağlamaz. Uygulama ileride Google Play'de yayımlanırsa bu sideload güncelleyicisi yerine Play'in uygulama içi güncelleme akışı kullanılmalıdır.
