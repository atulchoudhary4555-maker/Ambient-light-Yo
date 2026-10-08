# Ambient Light for YouTube

Ye Android 10+ app YouTube screen ke colors sample karke screen ke charon edges par soft ambient glow banata hai.

## GitHub se APK banane ka tarika
1. GitHub par new repository banayein: `AmbientLightYouTube`
2. Is project ki sari files upload karein.
3. `.github/workflows/build-apk.yml` bhi upload hona chahiye.
4. Repository me **Actions** tab kholen.
5. **Build Ambient Light APK** workflow select karke **Run workflow** dabayein.
6. Build complete hone par run ke bottom me **Artifacts** ke andar `AmbientLight-APK` milega.
7. ZIP artifact download karke `app-debug.apk` install karein.

App ko Android me **Display over other apps** aur screen-capture permission deni hogi.
