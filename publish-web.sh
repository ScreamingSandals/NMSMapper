#!/bin/bash

git config user.name github-actions[bot]
git config user.email 41898282+github-actions[bot]@users.noreply.github.com
git fetch origin gh-pages
git worktree add -f build/gitDocs gh-pages
cd build/gitDocs || exit 1
git rm -rf *
git checkout HEAD -- CNAME
cp -r ../docs/* .
git add .
git commit -m "Updated NMS Mapper website"
git push