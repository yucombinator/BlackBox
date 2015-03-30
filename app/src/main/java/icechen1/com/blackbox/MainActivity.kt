package icechen1.com.blackbox

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer
import it.neokree.materialnavigationdrawer.elements.MaterialSection

public class MainActivity : MaterialNavigationDrawer<Any>() {

    override fun init(bundle: Bundle?) {

        addSection(newSection(getResources().getString(R.string.list), MainActivityFragment()))
        enableToolbarElevation();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item!!.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
