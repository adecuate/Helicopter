package marsh.mallow.helicopter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Game extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // turn off title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // set full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(new GamePanel(this));
    }
}
