/*
 *    Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package sample.mindorks.com.nybus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.mindorks.nybus.NYBus;
import com.mindorks.nybus.annotations.Subscribe;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        NYBus.get().register(this);
        NYBus.get().post(new EventOne("08/01/1993","check"));
    }

    @Override
    protected void onStop() {
        NYBus.get().unregister(this);
        super.onStop();
    }

    @Subscribe
        public void onEvent(String a){
        Toast.makeText(this, "inside A", Toast.LENGTH_SHORT).show();
    }
    @Subscribe
    public void onNew(EventOne b){
        Toast.makeText(this, "inside B"+b.getName(), Toast.LENGTH_SHORT).show();
    }

}
