git checkout -b importing

git remote add remote_ecologylabSemanticService https://github.com/ecologylab/ecologylabSemanticService.git
git fetch remote_ecologylabSemanticService
git checkout -b branch_ecologylabSemanticService remote_ecologylabSemanticService/master
git checkout importing
git read-tree --prefix=bigSemanticsService/ -u branch_ecologylabSemanticService
git add bigSemanticsService
git commit -m "Imported ecologylabSemanticService as subproject bigSemanticsService."

git remote add remote_ecologylabSemanticsORM https://github.com/ecologylab/ecologylabSemanticsORM.git
git fetch remote_ecologylabSemanticsORM
git checkout -b branch_ecologylabSemanticsORM remote_ecologylabSemanticsORM/master
git checkout importing
git read-tree --prefix=bigSemanticsORM/ -u branch_ecologylabSemanticsORM
git add bigSemanticsORM
git commit -m "Imported ecologylabSemanticsORM as subproject bigSemanticsORM."

