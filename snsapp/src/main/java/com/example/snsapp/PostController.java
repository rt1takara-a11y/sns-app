package com.example.snsapp;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController: このクラスがWebリクエストを受け付けるコントローラーであることを示します。
// 各メソッドの返り値は、自動的にJSON形式のデータに変換されてクライアントに返されます。
@RestController
// @RequestMapping: このコントローラー内の全てのAPIのURLの接頭辞(プレフィックス)を指定します。
// ここでは、全てのURLが「/api/posts」から始まることになります。
@RequestMapping("/api/posts")
public class PostController {

    // final: この変数は一度初期化されたら変更できないことを示します。
    private final PostRepository postRepository;

    // コンストラクタ: PostControllerが作られる時に実行されるメソッドです。
    // Springが自動的にPostRepositoryのインスタンスをここに「注入」(DI)してくれます。
    // これにより、コントローラー内でリポジトリの機能が使えるようになります。
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // --- APIエンドポイントの定義 ---

    // @GetMapping: HTTPのGETリクエストをこのメソッドに割り当てます。
    // URLはベースの「/api/posts」になります。
    // [GET] /api/posts : 全ての投稿を取得する
    @GetMapping
    public List<Post> getAllPosts() {
        // リポジトリが持つfindAll()メソッドを呼び出して、全投稿をデータベースから取得します。
        return postRepository.findAll();
    }

    // @PostMapping: HTTPのPOSTリクエストをこのメソッドに割り当てます。
    // URLはベースの「/api/posts」になります。
    // [POST] /api/posts : 新しい投稿を作成する
    @PostMapping
    public Post createPost(@RequestBody Post newPost) {
        // @RequestBody: リクエストの本文(Body)に含まれるJSONデータを、
        // 自動的にPostオブジェクトに変換してくれます。
        // リポジトリのsave()メソッドで、受け取った投稿をデータベースに保存します。
        return postRepository.save(newPost);
    }

    // @DeleteMapping: HTTPのDELETEリクエストをこのメソッドに割り当てます。
    // "/{id}": URLの一部を可変なパラメータとして受け取ることを示します。
    // [DELETE] /api/posts/1 : IDが1の投稿を削除する
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        // @PathVariable: URLに含まれるパラメータ({id})をメソッドの引数として受け取ります。
        // リポジトリのdeleteById()メソッドで、指定されたIDの投稿を削除します。
        postRepository.deleteById(id);
    }
}