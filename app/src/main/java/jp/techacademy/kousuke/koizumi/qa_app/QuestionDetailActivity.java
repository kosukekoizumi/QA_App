package jp.techacademy.kousuke.koizumi.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mFavoriteRef; //★追加
    private DatabaseReference mAnswerRef;


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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton fabOff = (FloatingActionButton) findViewById(R.id.fabOff); //★お気に入り未登録時のボタン
        FloatingActionButton fabOn = (FloatingActionButton) findViewById(R.id.fabOn); //★お気に入り登録時のボタン

        if(user == null) {
           fabOff.setVisibility(View.GONE);
           fabOn.setVisibility(View.GONE);
        } else if ( 1 == 1 //★エラーをださないためのダミーの条件式
            //★Firebase読込み→お気に入り登録されているかの判断
            // DatabaseReference mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            // mFavoriteRef = mDataBaseReference.child(Const.FavoritePATH).child(user.get.Uid()).child(mQuestion.getQuestionUid());
            // mFavoriteRef.addChildEventListener(mFavoriteEventListener);
            // ★ここがうまくできない…
                ) {
            fabOff.setVisibility(View.VISIBLE); //★お気に入り未登録時のボタンを表示
            fabOn.setVisibility(View.GONE);
        } else {
            fabOn.setVisibility(View.VISIBLE); //★お気に入り登録時のボタンを表示
            fabOff.setVisibility(View.GONE);
        } //★"intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);"と"ClipData clipData = data.getClipData();"を使う？
          //★"try～catch"も使う？

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

        fabOff.setOnClickListener(new View.OnClickListener() { //★お気に入り未登録ボタンをクリック
            @Override
            public void onClick(View view) {
                //★Firebase書込み→お気に入り登録・保存
                // DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                // DatabaseReference genreRef = dataBaseReference.child(Const.FavoritePATH).child(String.valueOf(mGenre));
                // ★ここもうまくできない…
            }
        });

        fabOn.setOnClickListener(new View.OnClickListener() { //★お気に入り登録ボタンをクリック
            @Override
            public void onClick(View view) {
                //★Firebase書込み→お気に入り削除・保存
                // DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
                // DatabaseReference genreRef = dataBaseReference.child(Const.FavoritePATH).child(String.valueOf(mGenre));
                // ★ここもうまくできない…
            }
        });

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }
}