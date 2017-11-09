package jp.techacademy.kousuke.koizumi.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private DatabaseReference mFavoriteRef; //★追加
    private boolean favoriteFlag = false; //★追加
    private FloatingActionButton fabFavorite; //★追加
    private FirebaseUser mUser; //★追加

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }


        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        //★ボタン処理用のクラスブロック

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            favoriteFlag = true;

                    }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mUser = FirebaseAuth.getInstance().getCurrentUser(); //★追加

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fabFavorite = (FloatingActionButton) findViewById(R.id.fabFavorite); //★お気に入り登録ボタン定義

        if(mUser == null) {
            fabFavorite.setVisibility(View.GONE);
        } else {
            //★Firebase読込み→お気に入り登録されているかの判断
            DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            mFavoriteRef = mDataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
            mFavoriteRef.addChildEventListener(mFavoriteEventListener);

            fabFavorite.setVisibility(View.VISIBLE); //★お気に入り登録ボタン表示
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得するmk
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        fabFavorite.setOnClickListener(new View.OnClickListener() { //★お気に入り未登録ボタンをクリック
            @Override
            public void onClick(View view) {
                // ★お気に入り登録・解除を実施

                if (favoriteFlag == true) {
                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
                    favoriteRef.removeValue();

                    Snackbar.make(findViewById(android.R.id.content), "お気に入り解除しました。", Snackbar.LENGTH_LONG).show();

                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_on);

                    favoriteFlag = false;
                } else {
                    DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference favoriteRef = dataBaseReference.child(Const.FavoritePATH).child(mUser.getUid()).child(mQuestion.getQuestionUid());
                    Map<String, String> data = new HashMap<String, String>();
                    String genre = String.valueOf(mQuestion.getGenre());
                    data.put("genre", genre);
                    favoriteRef.setValue(data);

                    Snackbar.make(findViewById(android.R.id.content), "お気に入りに登録しました。", Snackbar.LENGTH_LONG).show();

                    fabFavorite.setImageResource(android.R.drawable.btn_star_big_off);
                    favoriteFlag = true;

                }
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}