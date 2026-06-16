package ru.berdinskiybear.armorhud.compat;

public interface ModCompat {
    float hudOpacity();
    int screenSafeArea();

    class NoOpModCompat implements ModCompat {
        @Override
        public float hudOpacity() {
            return 1;
        }

        @Override
        public int screenSafeArea() {
            return 0;
        }
    }
}
