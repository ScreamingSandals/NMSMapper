#!/bin/bash

git config user.name github-actions[bot]
git config user.email 41898282+github-actions[bot]@users.noreply.github.com
git fetch origin gh-pages
git worktree add -f build/gitDocs -
cd build/gitDocs || exit 1
git checkout --orphan tmp-pages
git checkout -m gh-pages CNAME
mv ../docs/* .
git add .
git commit -m "Updated NMSMapper docs"
git branch -D gh-pages
git branch -m gh-pages
git push -f origin gh-pages