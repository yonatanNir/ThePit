package shutterfly.joins.yonatanir.thepit;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import shutterfly.joins.yonatanir.thepit.Controls.Pit;

public class MainActivity extends AppCompatActivity
{
    private Button addPointButton;
    private Pit thePit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thePit = (Pit)findViewById(R.id.thePit);
        addPointButton = (Button)findViewById(R.id.buttonAddPoint);
        addPointButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(thePit != null)
                {
                    thePit.addNewPointToGraph();
                }
            }
        });

    }

}
